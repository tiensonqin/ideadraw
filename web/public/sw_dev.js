importScripts('https://storage.googleapis.com/workbox-cdn/releases/3.1.0/workbox-sw.js');

var prefix = "ideadraw";
var suffix = "v1";
if (workbox) {
  // console.log(`Yay! Workbox is loaded 🎉`);
  workbox.skipWaiting();
  workbox.clientsClaim();
  workbox.core.setCacheNameDetails({
    prefix: prefix,
    suffix: suffix
  });

  self.addEventListener('activate', (event) => {
    // Get a list of all the current open windows/tabs under
    // our service worker's control, and force them to reload.
    // This can "unbreak" any open windows/tabs as soon as the new
    // service worker activates, rather than users having to manually reload.

    self.clients.matchAll({ type: 'window' }).then(windowClients => {
      windowClients.forEach(windowClient => {
        windowClient.navigate(windowClient.url);
      });
    });

    event.waitUntil(
      caches.keys().then(function (keyList) {
        return Promise.all(keyList.map(function (key) {
          if ((key !== prefix + "-precache-" + suffix) && (key != prefix + "-runtime-" + suffix)) {
            return caches.delete(key);
          }
        }));
      })
    );
    return self.clients.claim();
  });

  workbox.routing.registerRoute(
    '/',
    workbox.strategies.networkOnly(),
  );

  // add directoryIndex null
  workbox.precaching.precacheAndRoute([
    {
      "url": "/",
      "revision": "2x7f4de41b6647e74c2556fadde42156"
    },
    {
      "url": "error.html",
      "revision": "207f4de41b6647e74c2556fadde42166"
    },
    {
      "url": "favicon.png",
      "revision": "c241d4024b4a177c5cc1f3c560692f25"
    },
    {
      "url": "images/logo-1x.png",
      "revision": "b88ed58a0493c5550ce4e26da5d9efab"
    },
    {
      "url": "images/logo-2x.png",
      "revision": "973ee13e14d3d93e5fd9801f543202b1"
    },
    {
      "url": "images/logo-3x.png",
      "revision": "2a2ab7d3e596400fa86b5a06c21d7c63"
    },
    {
      "url": "images/logo-4x.png",
      "revision": "6963a5022432158d8abdac152beb56f7"
    },
    {
      "url": "images/logo.png",
      "revision": "620c97b12b9b4d817fcbf4b13f5f5230"
    },
    {
      "url": "manifest.json",
      "revision": "2a90dfcb46e5be374c07b49a804f9559"
    },
    {
      "url": "robots.txt",
      "revision": "dd67f12c54d8d48417745d2b90220e0a"
    }
  ],
                                      {
                                        directoryIndex: null
                                      });

  workbox.routing.registerRoute(
    new RegExp('^https://fonts.(?:googleapis|gstatic).com/(.*)'),
    workbox.strategies.cacheFirst(),
  );

  // workbox.routing.registerRoute(
  //     /\.(?:js|css)$/,
  //   workbox.strategies.staleWhileRevalidate(),
  // );

  workbox.routing.registerRoute(
      /\.(?:png|gif|jpg|jpeg|svg)$/,
    workbox.strategies.cacheFirst({
      cacheName: 'images',
      plugins: [
        new workbox.expiration.Plugin({
          maxEntries: 60,
          maxAgeSeconds: 30 * 24 * 60 * 60, // 30 Days
        }),
      ],
    }),
  );

  workbox.googleAnalytics.initialize();

  workbox.routing.registerRoute(
    /.*(?:googleapis|gstatic)\.com.*$/,
    workbox.strategies.staleWhileRevalidate(),
  );

} else {
  console.log(`Boo! Workbox didn't load 😬`);
}
