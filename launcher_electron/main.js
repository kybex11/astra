const { app, BrowserWindow, ipcMain } = require('electron');
const path = require('path');
const net = require('net');
const { spawn } = require('child_process');
const fs = require('fs');

let mainWindow = null;
let discordSocket = null;
const CLIENT_ID = '1554130582190162051';
const startTime = Math.floor(Date.now() / 1000);

function getProjectRoot() {
  const candidates = [
    path.dirname(app.getPath('exe')),
    process.cwd(),
    path.resolve(__dirname, '..'),
    path.resolve(__dirname, '..', '..'),
    'A:\\datae\\evelent-core\\mc_cheat'
  ];

  for (const dir of candidates) {
    if (fs.existsSync(path.join(dir, 'gradlew.bat')) || fs.existsSync(path.join(dir, 'build.gradle'))) {
      return dir;
    }
  }
  return 'A:\\datae\\evelent-core\\mc_cheat';
}

function initDiscordRPC() {
  for (let i = 0; i < 10; i++) {
    const pipePath = `\\\\.\\pipe\\discord-ipc-${i}`;
    try {
      const sock = net.createConnection(pipePath);
      sock.on('connect', () => {
        discordSocket = sock;
        const handshake = JSON.stringify({ v: 1, client_id: CLIENT_ID });
        sendDiscordPacket(0, handshake);
        updateDiscordPresence('В лаунчере Astra', 'Astra Client 1.16.5');
      });
      sock.on('error', () => {});
      if (discordSocket) break;
    } catch (e) {}
  }
}

function sendDiscordPacket(op, jsonStr) {
  if (!discordSocket) return;
  const payload = Buffer.from(jsonStr, 'utf-8');
  const header = Buffer.alloc(8);
  header.writeUInt32LE(op, 0);
  header.writeUInt32LE(payload.length, 4);
  discordSocket.write(Buffer.concat([header, payload]));
}

function updateDiscordPresence(state, details) {
  if (!discordSocket) return;
  const payload = JSON.stringify({
    cmd: 'SET_ACTIVITY',
    args: {
      pid: process.pid,
      activity: {
        state: state,
        details: details,
        timestamps: { start: startTime },
        assets: {
          large_image: 'icon',
          large_text: 'Astra Client 1.16.5'
        }
      }
    },
    nonce: String(Date.now())
  });
  sendDiscordPacket(1, payload);
}

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 840,
    height: 560,
    minWidth: 780,
    minHeight: 520,
    frame: false,
    transparent: false,
    backgroundColor: '#0B0F19',
    title: 'Astra Launcher',
    webPreferences: {
      nodeIntegration: true,
      contextIsolation: false
    }
  });

  mainWindow.loadFile(path.join(__dirname, 'index.html'));
  initDiscordRPC();
}

app.whenReady().then(createWindow);

app.on('window-all-closed', () => {
  if (discordSocket) discordSocket.destroy();
  if (process.platform !== 'darwin') app.quit();
});

// IPC Handlers
ipcMain.on('window-min', () => {
  if (mainWindow) mainWindow.minimize();
});

ipcMain.on('window-close', () => {
  if (mainWindow) mainWindow.close();
});

ipcMain.on('launch-game', (event, { username }) => {
  try {
    const projectRoot = getProjectRoot();
    const playerNick = username || 'AstraPlayer';

    updateDiscordPresence(`В игре: ${playerNick}`, 'Astra Client 1.16.5');

    const logFile = path.join(projectRoot, 'run', 'logs', 'launcher_game.log');
    const logDir = path.dirname(logFile);
    if (!fs.existsSync(logDir)) fs.mkdirSync(logDir, { recursive: true });
    const logStream = fs.createWriteStream(logFile, { flags: 'w' });

    const gradlewPath = path.join(projectRoot, 'gradlew.bat');
    const childEnv = { ...process.env };
    const bundledJava = path.join(projectRoot, 'runtime', 'java');
    const altBundledJava = path.join(path.dirname(app.getPath('exe')), 'runtime', 'java');

    if (fs.existsSync(bundledJava)) {
      childEnv['JAVA_HOME'] = bundledJava;
      childEnv['PATH'] = path.join(bundledJava, 'bin') + ';' + (process.env.PATH || '');
    } else if (fs.existsSync(altBundledJava)) {
      childEnv['JAVA_HOME'] = altBundledJava;
      childEnv['PATH'] = path.join(altBundledJava, 'bin') + ';' + (process.env.PATH || '');
    }

    const gameProcess = spawn('cmd.exe', ['/c', gradlewPath, 'runClient', `-Pusername=${playerNick}`], {
      cwd: projectRoot,
      env: childEnv,
      detached: true,
      windowsHide: true,
      stdio: ['ignore', 'pipe', 'pipe']
    });

    gameProcess.stdout.pipe(logStream);
    gameProcess.stderr.pipe(logStream);

    gameProcess.on('error', (err) => {
      console.error('Failed to launch game process:', err);
      event.reply('launch-status', { success: false, message: 'Ошибка запуска: ' + err.message });
    });

    event.reply('launch-status', { success: true, message: 'Minecraft 1.16.5 запускается...' });

    gameProcess.on('exit', (code) => {
      if (code !== 0 && code !== null) {
        event.reply('launch-status', { success: false, message: `Процесс завершился с кодом ${code}.` });
      }
    });

    gameProcess.unref();
  } catch (err) {
    console.error('Launch exception:', err);
    event.reply('launch-status', { success: false, message: 'Исключение: ' + err.message });
  }
});
