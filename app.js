// J.A.R.V.I.S. Progressive Web App Core Engine
let deferredPrompt = null;
let isListening = false;
let recognition = null;

// Register Service Worker for PWA installability
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('./sw.js')
      .then((reg) => console.log('J.A.R.V.I.S. SW Registered', reg.scope))
      .catch((err) => console.error('SW Registration Failed', err));
  });
}

// 1. Capture PWA Direct Install Prompt Event
window.addEventListener('beforeinstallprompt', (e) => {
  e.preventDefault();
  deferredPrompt = e;
  console.log('beforeinstallprompt captured!');
  const installBtn = document.getElementById('btn-pwa-install');
  const installBadge = document.getElementById('install-badge');
  if (installBtn) {
    installBtn.style.display = 'flex';
    installBtn.innerHTML = `
      <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z"/></svg>
      INSTALL J.A.R.V.I.S. DIRECT TO HOME SCREEN
    `;
  }
  if (installBadge) {
    installBadge.textContent = 'READY TO INSTALL';
  }
});

window.addEventListener('appinstalled', () => {
  console.log('J.A.R.V.I.S. Web App was successfully installed directly!');
  deferredPrompt = null;
  const installBtn = document.getElementById('btn-pwa-install');
  const installBadge = document.getElementById('install-badge');
  if (installBtn) {
    installBtn.innerHTML = `✓ INSTALLED DIRECTLY`;
    installBtn.style.background = '#00FFCC';
  }
  if (installBadge) {
    installBadge.textContent = 'INSTALLED';
  }
});

// Check if running in standalone PWA mode
if (window.matchMedia('(display-mode: standalone)').matches || window.navigator.standalone === true) {
  const installBadge = document.getElementById('install-badge');
  const installBtn = document.getElementById('btn-pwa-install');
  if (installBadge) installBadge.textContent = 'RUNNING AS STANDALONE APP';
  if (installBtn) {
    installBtn.innerHTML = `✓ ACTIVE AS STANDALONE APP`;
    installBtn.style.background = '#00FFCC';
  }
}

// Direct Install Button Click Handler
document.addEventListener('DOMContentLoaded', () => {
  const installBtn = document.getElementById('btn-pwa-install');
  if (installBtn) {
    installBtn.addEventListener('click', async () => {
      if (deferredPrompt) {
        deferredPrompt.prompt();
        const choice = await deferredPrompt.userChoice;
        console.log('User choice outcome:', choice.outcome);
        if (choice.outcome === 'accepted') {
          installBtn.innerHTML = '✓ INSTALLING DIRECTLY...';
        }
        deferredPrompt = null;
      } else {
        // Fallback for browsers when beforeinstallprompt is already handled or not yet triggered
        alert("To install J.A.R.V.I.S. direct from this web app:\n\n1. In Chrome: Tap the (⋮) menu in top right -> Tap 'Install app' or 'Add to Home screen'\n2. In Safari: Tap the Share button -> Tap 'Add to Home Screen'\n\nJ.A.R.V.I.S. will install as a standalone app!");
      }
    });
  }

  // Setup Web Speech Recognition
  setupSpeechEngine();
});

function setupSpeechEngine() {
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
  const reactor = document.getElementById('reactor-orb');
  const statusTitle = document.getElementById('speech-title');
  const statusSub = document.getElementById('speech-sub');
  const responseBox = document.getElementById('response-box');

  if (!SpeechRecognition) {
    if (statusSub) statusSub.textContent = "Web Speech API not supported on this browser. Type directives below.";
    return;
  }

  recognition = new SpeechRecognition();
  recognition.continuous = false;
  recognition.interimResults = true;
  recognition.lang = 'en-US';

  recognition.onstart = () => {
    isListening = true;
    if (reactor) reactor.classList.add('listening');
    if (statusTitle) statusTitle.textContent = "LISTENING FOR DIRECTIVE...";
    if (statusSub) statusSub.textContent = "Speak now, Sir...";
  };

  recognition.onresult = (event) => {
    const transcript = Array.from(event.results)
      .map(r => r[0].transcript)
      .join('');
    if (statusSub) statusSub.textContent = `"${transcript}"`;

    if (event.results[0].isFinal) {
      processWebDirective(transcript);
    }
  };

  recognition.onerror = (event) => {
    console.warn('Speech recognition error:', event.error);
    isListening = false;
    if (reactor) reactor.classList.remove('listening');
    if (statusTitle) statusTitle.textContent = "ARC REACTOR READY";
    if (statusSub) statusSub.textContent = "Tap core to speak directive";
  };

  recognition.onend = () => {
    isListening = false;
    if (reactor) reactor.classList.remove('listening');
    if (statusTitle && statusTitle.textContent === "LISTENING FOR DIRECTIVE...") {
      statusTitle.textContent = "ARC REACTOR READY";
      statusSub.textContent = "Tap core to speak directive";
    }
  };

  if (reactor) {
    reactor.addEventListener('click', () => {
      if (isListening) {
        recognition.stop();
      } else {
        try {
          recognition.start();
        } catch (e) {
          console.error(e);
        }
      }
    });
  }
}

function processWebDirective(command) {
  const statusTitle = document.getElementById('speech-title');
  const responseText = document.getElementById('response-text');
  const actionTag = document.getElementById('action-tag');

  if (statusTitle) statusTitle.textContent = "PROCESSING VIA GEMINI...";

  // Simulated Gemini Intent Analysis for Web Companion
  const lower = command.toLowerCase();
  let response = "All systems nominal, Sir. Telemetry indicates optimal functioning across all sectors.";
  let tag = "STATUS_REPORT";

  if (lower.includes("calendar") || lower.includes("meeting") || lower.includes("schedule")) {
    response = "You have 3 upcoming events today: Stark Industries Board Briefing at 10:00 AM, Mark LXXXV Armor Calibration at 2:00 PM, and Security Brief with Avengers at 6:00 PM.";
    tag = "CALENDAR_EVENTS";
  } else if (lower.includes("light") || lower.includes("lamp")) {
    response = "Smart home lighting protocols executed. Malibu mansion illumination adjusted to 75% warm ambient.";
    tag = "SMART_LIGHTS";
  } else if (lower.includes("who are you") || lower.includes("jarvis")) {
    response = "I am J.A.R.V.I.S. Just A Rather Very Intelligent System. Operational across mobile, web, and all background applications.";
    tag = "ASSISTANT_IDENTITY";
  } else {
    response = `Directive received: "${command}". Stark OS intelligence analyzed and synchronized your directive.`;
    tag = "GEMINI_DIRECTIVE";
  }

  if (responseText) responseText.textContent = `"${response}"`;
  if (actionTag) actionTag.textContent = tag;
  if (statusTitle) statusTitle.textContent = "DIRECTIVE EXECUTED";

  // Vocalize via Web SpeechSynthesis
  if ('speechSynthesis' in window) {
    const utter = new SpeechSynthesisUtterance(response);
    utter.rate = 1.05;
    utter.pitch = 0.95;
    window.speechSynthesis.speak(utter);
  }
}
