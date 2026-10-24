const { app, BrowserWindow, ipcMain, shell } = require('electron');
const path = require('path');
const net = require('net');
const { spawn, execFileSync } = require('child_process');
const fs = require('fs');

let mainWindow = null;
let discordSocket = null;
let gameProcess = null;
const CLIENT_ID = '1554130582190162051';
const startTime = Math.floor(Date.now() / 1000);

function getProjectRoot() {
  const candidates = [];
  try { candidates.push(path.dirname(app.getPath('exe'))); } catch (e) {}
  try { candidates.push(path.join(path.dirname(app.getPath('exe')), '..')); } catch (e) {}
  try { candidates.push(path.join(path.dirname(app.getPath('exe')), '..', '..')); } catch (e) {}
  try { if (process.resourcesPath) candidates.push(process.resourcesPath); } catch (e) {}
  try { candidates.push(path.resolve(__dirname, '..')); } catch (e) {}
  try { candidates.push(path.resolve(__dirname, '..', '..')); } catch (e) {}
  try { candidates.push(process.cwd()); } catch (e) {}
  candidates.push('A:\\datae\\evelent-core\\mc_cheat');

  const seen = new Set();
  for (const dir of candidates) {
    if (!dir || seen.has(dir)) continue;
    seen.add(dir);
    try {
      if (fs.existsSync(path.join(dir, 'gradlew.bat'))) return dir;
    } catch (e) {}
  }
  // Second pass: packaged app without gradle sources (production fallback)
  for (const dir of candidates) {
    if (!dir) continue;
    try {
      if (fs.existsSync(path.join(dir, 'installer', 'astra-1.0.0.jar'))) return dir;
      if (fs.existsSync(path.join(dir, 'resources', 'astra-1.0.0.jar'))) return dir;
    } catch (e) {}
  }
  return candidates[candidates.length - 1];
}

function findJava17(projectRoot) {
  const candidates = [];
  // Bundled JRE next to project / launcher
  try {
    candidates.push(path.join(projectRoot, 'runtime', 'java', 'bin', 'java.exe'));
    candidates.push(path.join(path.dirname(app.getPath('exe')), 'runtime', 'java', 'bin', 'java.exe'));
    if (process.resourcesPath) candidates.push(path.join(process.resourcesPath, 'runtime', 'java', 'bin', 'java.exe'));
  } catch (e) {}
  if (process.env.JAVA17_HOME) candidates.push(path.join(process.env.JAVA17_HOME, 'bin', 'java.exe'));
  // Common Adoptium install locations
  candidates.push('C:\\Program Files\\Eclipse Adoptium\\jdk-17.0.18.8-hotspot\\bin\\java.exe');
  const pf = process.env['ProgramFiles'];
  if (pf) {
    try {
      const dir = path.join(pf, 'Eclipse Adoptium');
      if (fs.existsSync(dir)) {
        for (const entry of fs.readdirSync(dir)) {
          if (/^jdk-17/i.test(entry)) candidates.push(path.join(dir, entry, 'bin', 'java.exe'));
        }
      }
    } catch (e) {}
  }

  for (const exe of candidates) {
    try { if (exe && fs.existsSync(exe)) return exe; } catch (e) {}
  }
  // Fallback: java from PATH, but only if it is version 17
  try {
    const out = execFileSync('java', ['-version'], { stdio: ['ignore', 'pipe', 'pipe'] }).toString()
      + execFileSync('java', ['-XshowSettings:properties', '-version'], { stdio: ['ignore', 'pipe', 'pipe'] }).toString();
    void out;
  } catch (e) {
    const stderr = (e && e.stderr ? e.stderr.toString() : '') + (e && e.stdout ? e.stdout.toString() : '');
    const m = stderr.match(/version\s+"(\d+)/);
    if (m && m[1] === '17') return 'java';
    // java exists but wrong version -> do not use blindly
    if (m) return null;
  }
  try {
    execFileSync('where', ['java'], { stdio: ['ignore', 'pipe', 'pipe'] });
    return null; // exists but version unknown/wrong -> caller will report it
  } catch (e) {
    return null;
  }
}

function javaHomeOf(javaExe) {
  try {
    if (!javaExe || javaExe === 'java') return null;
    return path.dirname(path.dirname(javaExe));
  } catch (e) { return null; }
}

function sanitizeNick(nick) {
  const s = String(nick || '').trim();
  if (/^[A-Za-z0-9_]{3,16}$/.test(s)) return s;
  return 'AstraPlayer';
}

function clampRam(ram) {
  const n = parseInt(ram, 10);
  if (!Number.isFinite(n)) return 4;
  return Math.min(12, Math.max(2, n));
}

function initDiscordRPC() {
  // Try discord IPC pipes sequentially, stop at first successful handshake.
  let attempt = 0;
  const tryPipe = () => {
    if (discordSocket || attempt >= 10) return;
    const pipePath = `\\\\.\\pipe\\discord-ipc-${attempt++}`;
    let sock = null;
    try {
      sock = net.createConnection(pipePath);
    } catch (e) {
      setTimeout(tryPipe, 300);
      return;
    }
    const onError = () => {
      try { sock.destroy(); } catch (e) {}
      setTimeout(tryPipe, 300);
    };
    sock.on('error', onError);
    sock.on('connect', () => {
      if (discordSocket) { try { sock.destroy(); } catch (e) {} return; }
      discordSocket = sock;
      sock.removeListener('error', onError);
      sock.on('error', () => {});
      sock.on('close', () => { if (discordSocket === sock) discordSocket = null; });
      const handshake = JSON.stringify({ v: 1, client_id: CLIENT_ID });
      sendDiscordPacket(0, handshake);
      updateDiscordPresence('В лаунчере Astra', 'Astra Client 1.16.5');
    });
  };
  tryPipe();
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

ipcMain.on('open-folder', (event, type) => {
  try {
    if (type === 'mods') {
      const p = path.join(process.env.APPDATA || '', '.minecraft', 'mods');
      if (!fs.existsSync(p)) fs.mkdirSync(p, { recursive: true });
      shell.openPath(p);
    } else if (type === 'logs') {
      const p = path.join(getProjectRoot(), 'run', 'logs');
      if (!fs.existsSync(p)) fs.mkdirSync(p, { recursive: true });
      shell.openPath(p);
    } else if (type === 'root') {
      shell.openPath(getProjectRoot());
    }
  } catch (err) {
    console.error('Failed to open folder:', err);
  }
});

function sendStatus(msg) {
  try {
    if (mainWindow && !mainWindow.isDestroyed()) mainWindow.webContents.send('launch-status', msg);
  } catch (e) {}
}

function findModJar(projectRoot) {
  const cands = [
    path.join(projectRoot, 'build', 'libs', 'astra-1.0.0.jar'),
    path.join(projectRoot, 'installer', 'astra-1.0.0.jar'),
    path.join(projectRoot, 'resources', 'astra-1.0.0.jar'),
  ];
  try { if (process.resourcesPath) cands.push(path.join(process.resourcesPath, 'astra-1.0.0.jar')); } catch (e) {}
  for (const c of cands) {
    try { if (fs.existsSync(c)) return c; } catch (e) {}
  }
  return null;
}

ipcMain.on('launch-game', (event, { username, ram }) => {
  try {
    if (gameProcess && gameProcess.exitCode === null) {
      sendStatus({ success: false, message: 'Игра уже запускается, подождите...' });
      return;
    }

    const projectRoot = getProjectRoot();
    const playerNick = sanitizeNick(username);
    const ramGb = clampRam(ram);

    updateDiscordPresence(`В игре: ${playerNick}`, 'Astra Client 1.16.5');

    const gradlewPath = path.join(projectRoot, 'gradlew.bat');
    const hasGradleProject = fs.existsSync(gradlewPath) && fs.existsSync(path.join(projectRoot, 'build.gradle'));

    // ---- Production fallback: no gradle sources (installed launcher on friend PC) ----
    if (!hasGradleProject) {
      const modJar = findModJar(projectRoot);
      const modsDir = path.join(process.env.APPDATA || path.join(process.env.USERPROFILE || '', 'AppData', 'Roaming'), '.minecraft', 'mods');
      let copied = false;
      try {
        if (modJar && fs.existsSync(modsDir)) {
          fs.copyFileSync(modJar, path.join(modsDir, 'astra-1.0.0.jar'));
          copied = true;
        } else if (modJar) {
          fs.mkdirSync(modsDir, { recursive: true });
          fs.copyFileSync(modJar, path.join(modsDir, 'astra-1.0.0.jar'));
          copied = true;
        }
      } catch (e) { copied = false; }
      sendStatus({
        success: false,
        message: copied
          ? 'Мод скопирован в .minecraft/mods. Выберите Fabric 1.16.5 в лаунчере Minecraft и нажмите Играть (RShift — меню).'
          : 'Не найден gradle-проект. Установите Fabric Loader 1.16.5 и скопируйте astra-1.0.0.jar из installer/ в %APPDATA%/.minecraft/mods.'
      });
      return;
    }

    const logFile = path.join(projectRoot, 'run', 'logs', 'launcher_game.log');
    const logDir = path.dirname(logFile);
    if (!fs.existsSync(logDir)) fs.mkdirSync(logDir, { recursive: true });

    const javaExe = findJava17(projectRoot);
    const javaHome = javaHomeOf(javaExe);
    if (!javaExe) {
      sendStatus({ success: false, message: 'Нужна Java 17 (не найдена). Установите Eclipse Temurin 17 и перезапустите лаунчер.' });
      return;
    }

    const childEnv = { ...process.env };
    if (javaHome) {
      childEnv['JAVA_HOME'] = javaHome;
      childEnv['PATH'] = path.join(javaHome, 'bin') + ';' + (process.env.PATH || '');
    }

    const args = ['runClient', `-Pusername=${playerNick}`, `-PmcRam=${ramGb}`, `-PmcJava=${javaExe}`, '-Dorg.gradle.java.home=' + javaHome];
    const headerLines = [
      `==== Astra Launcher ====`,
      `Date: ${new Date().toISOString()}`,
      `Nick: ${playerNick}`,
      `RAM: ${ramGb}G`,
      `Java: ${javaExe}`,
      `Cmd: "${gradlewPath}" ${args.join(' ')}`,
      `Cwd: ${projectRoot}`,
      `========================`,
      ``
    ];
    const logStream = fs.createWriteStream(logFile, { flags: 'w' });
    logStream.write(headerLines.join('\n'));

    const tailLines = [];
    const pushTail = (chunk) => {
      const parts = String(chunk).split(/\r?\n/);
      for (const p of parts) {
        if (p.trim() !== '') {
          tailLines.push(p);
          if (tailLines.length > 80) tailLines.shift();
        }
      }
    };
    const extractError = () => {
      // Prefer gradle error lines
      const interesting = tailLines.filter((l) => /error|exception|failed|FAILED|not found|No such|Could not/i.test(l));
      const src = interesting.length ? interesting.slice(-6) : tailLines.slice(-6);
      return src.join('\n').slice(0, 900);
    };

    sendStatus({ success: true, message: `Minecraft 1.16.5 запускается (${playerNick}, ${ramGb} GB)...` });

    gameProcess = spawn(gradlewPath, args, {
      cwd: projectRoot,
      env: childEnv,
      shell: true,
      windowsHide: true,
      stdio: ['ignore', 'pipe', 'pipe']
    });

    gameProcess.stdout.on('data', (d) => {
      try { logStream.write(d); } catch (e) {}
      pushTail(d);
      try { if (mainWindow && !mainWindow.isDestroyed()) mainWindow.webContents.send('launch-log', d.toString()); } catch (e) {}
    });
    gameProcess.stderr.on('data', (d) => {
      try { logStream.write(d); } catch (e) {}
      pushTail(d);
      try { if (mainWindow && !mainWindow.isDestroyed()) mainWindow.webContents.send('launch-log', d.toString()); } catch (e) {}
    });

    gameProcess.on('error', (err) => {
      try { logStream.write(`\n[LAUNCHER] spawn error: ${err && err.message}\n`); } catch (e) {}
      try { logStream.end(); } catch (e) {}
      sendStatus({ success: false, message: 'Ошибка запуска: ' + (err && err.message) });
      gameProcess = null;
    });

    gameProcess.on('exit', (code, signal) => {
      try { logStream.write(`\n[LAUNCHER] process exit code=${code} signal=${signal}\n`); } catch (e) {}
      try { logStream.end(); } catch (e) {}
      const finished = gameProcess;
      gameProcess = null;
      void finished;
      if (code === 0 || code === null) {
        sendStatus({ success: true, message: 'Игра запущена.' });
      } else {
        const details = extractError();
        sendStatus({ success: false, message: `Вылет с кодом ${code}. ${details ? 'Лог: ' + details : 'Смотри run/logs/launcher_game.log'}` });
      }
    });
  } catch (err) {
    console.error('Launch exception:', err);
    sendStatus({ success: false, message: 'Исключение: ' + (err && err.message) });
  }
});
