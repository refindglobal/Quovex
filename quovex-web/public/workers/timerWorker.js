/**
 * Dedicated Web Worker for Quovex Focus Timer
 * Ensures accurate seconds ticking even when the browser tab is backgrounded or throttled.
 */

let timerId = null;

self.onmessage = function (e) {
  const { action, interval = 1000 } = e.data;

  if (action === 'START') {
    if (timerId) clearInterval(timerId);
    timerId = setInterval(() => {
      self.postMessage({ type: 'TICK', timestamp: Date.now() });
    }, interval);
  } else if (action === 'STOP' || action === 'PAUSE') {
    if (timerId) {
      clearInterval(timerId);
      timerId = null;
    }
  }
};
