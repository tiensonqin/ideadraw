(ns share.icons
  (:require [share.util :as util]))

(def icons
  {:search (fn [{:keys [width height fill]}]
             (util/format "<svg fill=\"%s\" height=\"%d\" viewBox=\"0 0 24 24\" width=\"%d\" xmlns=\"http://www.w3.org/2000/svg\">
    <path d=\"M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z\"/>
    <path d=\"M0 0h24v24H0z\" fill=\"none\"/>
</svg>"
                          fill width height))

   :close (fn [{:keys [width height fill]}]
            (util/format "<svg fill=\"%s\" xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" viewBox=\"0 0 24 24\"><path d=\"M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z\"/></svg>"
                         fill width height))

   :favorite (fn [{:keys [width height fill]}]
               (util/format "<svg fill=\"%s\" xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" viewBox=\"0 0 24 24\"><path d=\"M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z\"/></svg>"
                            fill width height))

   :favorite_border (fn [{:keys [width height fill]}]
                      (util/format
                       "<svg fill=\"%s\" xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" viewBox=\"0 0 24 24\"><path d=\"M16.5 3c-1.74 0-3.41.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3 4.42 3 2 5.42 2 8.5c0 3.78 3.4 6.86 8.55 11.54L12 21.35l1.45-1.32C18.6 15.36 22 12.28 22 8.5 22 5.42 19.58 3 16.5 3zm-4.4 15.55l-.1.1-.1-.1C7.14 14.24 4 11.39 4 8.5 4 6.5 5.5 5 7.5 5c1.54 0 3.04.99 3.57 2.36h1.87C13.46 5.99 14.96 5 16.5 5c2 0 3.5 1.5 3.5 3.5 0 2.89-3.14 5.74-7.9 10.05z\"/></svg>"
                       fill width height))
   :flag (fn [{:keys [width height fill]}]
           (util/format
            "<svg fill=\"%s\" xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" viewBox=\"0 0 24 24\"><path d=\"M14.4 6L14 4H5v17h2v-7h5.6l.4 2h7V6z\"/></svg>"
            fill width height))
   :reply (fn [{:keys [width height fill]}]
            (util/format
             "<svg fill=\"%s\" xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" viewBox=\"0 0 24 24\">
    <path d=\"M10 9V5l-7 7 7 7v-4.1c5 0 8.5 1.6 11 5.1-1-5-4-10-11-11z\"/>
    <path d=\"M0 0h24v24H0z\" fill=\"none\"/>
</svg>
"
             fill width height))
   :keyboard_arrow_up (fn [{:keys [width height fill]}]
                        (util/format
                         "<svg fill=\"%s\" xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" viewBox=\"0 0 24 24\">
    <path fill=\"none\" d=\"M0 0h24v24H0V0z\"/>
    <path d=\"M4 12l1.41 1.41L11 7.83V20h2V7.83l5.58 5.59L20 12l-8-8-8 8z\"/>
</svg>
"
                         fill width height))
   :whatshot (fn [{:keys [width height fill]}]
               (util/format
                "<svg xmlns=\"http://www.w3.org/2000/svg\" fill=\"%s\" width=\"%d\" height=\"%d\" viewBox=\"0 0 24 24\">
    <path d=\"M13.5.67s.74 2.65.74 4.8c0 2.06-1.35 3.73-3.41 3.73-2.07 0-3.63-1.67-3.63-3.73l.03-.36C5.21 7.51 4 10.62 4 14c0 4.42 3.58 8 8 8s8-3.58 8-8C20 8.61 17.41 3.8 13.5.67zM11.71 19c-1.78 0-3.22-1.4-3.22-3.14 0-1.62 1.05-2.76 2.81-3.12 1.77-.36 3.6-1.21 4.62-2.58.39 1.29.59 2.65.59 4.04 0 2.65-2.15 4.8-4.8 4.8z\"/>
    <path d=\"M0 0h24v24H0z\" fill=\"none\"/>
</svg>
"
                fill width height))
   :expand_less (fn [{:keys [width height fill]}]
                  (util/format
                   "<svg fill=\"%s\" xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" viewBox=\"0 0 24 24\">
    <path d=\"M12 8l-6 6 1.41 1.41L12 10.83l4.59 4.58L18 14z\"/>
    <path d=\"M0 0h24v24H0z\" fill=\"none\"/>
</svg>
"
                   fill width height))

   :expand_more (fn [{:keys [width height fill]}]
                  (util/format
                   "<svg fill=\"%s\" xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" viewBox=\"0 0 24 24\">
    <path d=\"M16.59 8.59L12 13.17 7.41 8.59 6 10l6 6 6-6z\"/>
    <path d=\"M0 0h24v24H0z\" fill=\"none\"/>
</svg>
"
                   fill width height))
   :edit (fn [{:keys [width height fill]}]
           (util/format
            "<svg fill=\"%s\" xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" viewBox=\"0 0 24 24\">
    <path d=\"M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z\"/>
    <path d=\"M0 0h24v24H0z\" fill=\"none\"/>
</svg>
"
            fill width height))
   :notifications (fn [{:keys [width height fill]}]
                    (util/format
                     "<svg fill=\"%s\" width=\"%d\" height=\"%d\" viewBox=\"0 0 24 24\" xmlns=\"http://www.w3.org/2000/svg\">
    <path d=\"M12 22c1.1 0 2-.9 2-2h-4c0 1.1.89 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z\"/>
</svg>
"
                     fill width height))
   :menu (fn [{:keys [width height fill]}]
           (util/format
            "<svg fill=\"%s\" xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" viewBox=\"0 0 24 24\">
    <path d=\"M0 0h24v24H0z\" fill=\"none\"/>
    <path d=\"M3 18h18v-2H3v2zm0-5h18v-2H3v2zm0-7v2h18V6H3z\"/>
</svg>
"
            fill width height))
   :mail (fn [{:keys [width height fill]}]
           (util/format
            "<svg fill=\"%s\" xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" viewBox=\"0 0 24 24\">
    <path d=\"M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z\"/>
    <path d=\"M0 0h24v24H0z\" fill=\"none\"/>
</svg>
"
            fill width height))
   :add_circle_outline (fn [{:keys [width height fill]}]
                         (util/format
                          "<svg fill=\"%s\" width=\"%d\" height=\"%d\" xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 24 24\">
    <path d=\"M0 0h24v24H0z\" fill=\"none\"/>
    <path d=\"M13 7h-2v4H7v2h4v4h2v-4h4v-2h-4V7zm-1-5C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z\"/>
</svg>
"
                          fill width height))
   :add (fn [{:keys [width height fill]}]
          (util/format
           "<svg fill=\"%s\" xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" viewBox=\"0 0 24 24\">
    <path d=\"M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z\"/>
    <path d=\"M0 0h24v24H0z\" fill=\"none\"
/>
</svg>
"
           fill width height))
   :send (fn [{:keys [width height fill]}]
           (util/format
            "<svg fill=\"%s\" xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" viewBox=\"0 0 24 24\">
    <path d=\"M2.01 21L23 12 2.01 3 2 10l15 2-15 2z\"/>
    <path d=\"M0 0h24v24H0z\" fill=\"none\"/>
</svg>
"
            fill width height))
   :add_a_photo (fn [{:keys [width height fill]}]
                  (util/format
                   "<svg fill=\"%s\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" width=\"%d\" height=\"%d\" viewBox=\"0 0 24 24\">
    <defs>
        <path id=\"a\" d=\"M24 24H0V0h24v24z\"/>
    </defs>
    <clipPath id=\"b\">
        <use xlink:href=\"#a\" overflow=\"visible\"/>
    </clipPath>
    <path clip-path=\"url(#b)\" d=\"M3 4V1h2v3h3v2H5v3H3V6H0V4h3zm3 6V7h3V4h7l1.83 2H21c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H5c-1.1 0-2-.9-2-2V10h3zm7 9c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-3.2-5c0 1.77 1.43 3.2 3.2 3.2s3.2-1.43 3.2-3.2-1.43-3.2-3.2-3.2-3.2 1.43-3.2 3.2z\"/>
</svg>
"
                   fill width height))
   :visibility (fn [{:keys [width height fill]}]
                 (util/format
                  "<svg fill=\"%s\" xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" viewBox=\"0 0 24 24\">
    <path d=\"M0 0h24v24H0z\" fill=\"none\"/>
    <path d=\"M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z\"/>
</svg>
"
                  fill width height))
   :comments (fn [{:keys [width height fill]}]
               (util/format
                "<svg fill=\"%s\" xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" viewBox=\"0 0 24 24\">
    <path d=\"M21.99 4c0-1.1-.89-2-1.99-2H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h14l4 4-.01-18zM18 14H6v-2h12v2zm0-3H6V9h12v2zm0-3H6V6h12v2z\"/>
    <path d=\"M0 0h24v24H0z\" fill=\"none\"/>
</svg>
"
                fill width height))
   :link (fn [{:keys [width height fill]}]
           (util/format
            "<svg fill=\"%s\" xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" viewBox=\"0 0 24 24\">
    <path d=\"M0 0h24v24H0z\" fill=\"none\"/>
    <path d=\"M3.9 12c0-1.71 1.39-3.1 3.1-3.1h4V7H7c-2.76 0-5 2.24-5 5s2.24 5 5 5h4v-1.9H7c-1.71 0-3.1-1.39-3.1-3.1zM8 13h8v-2H8v2zm9-6h-4v1.9h4c1.71 0 3.1 1.39 3.1 3.1s-1.39 3.1-3.1 3.1h-4V17h4c2.76 0 5-2.24 5-5s-2.24-5-5-5z\"/>
</svg>
"
            fill width height))
   :chat_bubble_outline (fn [{:keys [width height fill]}]
                          (util/format
                           "<svg fill=\"%s\" xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" viewBox=\"0 0 24 24\">
    <path fill=\"none\" d=\"M0 0h24v24H0V0z\"/>
    <path d=\"M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z\"/>
</svg>
"
                           fill width height))
   :settings (fn [{:keys [width height fill]}]
               (util/format
                "<svg fill=\"%s\" xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" viewBox=\"0 0 24 24\">
    <path d=\"M0 0h24v24H0z\" fill=\"none\"/>
    <path d=\"M19.43 12.98c.04-.32.07-.64.07-.98s-.03-.66-.07-.98l2.11-1.65c.19-.15.24-.42.12-.64l-2-3.46c-.12-.22-.39-.3-.61-.22l-2.49 1c-.52-.4-1.08-.73-1.69-.98l-.38-2.65C14.46 2.18 14.25 2 14 2h-4c-.25 0-.46.18-.49.42l-.38 2.65c-.61.25-1.17.59-1.69.98l-2.49-1c-.23-.09-.49 0-.61.22l-2 3.46c-.13.22-.07.49.12.64l2.11 1.65c-.04.32-.07.65-.07.98s.03.66.07.98l-2.11 1.65c-.19.15-.24.42-.12.64l2 3.46c.12.22.39.3.61.22l2.49-1c.52.4 1.08.73 1.69.98l.38 2.65c.03.24.24.42.49.42h4c.25 0 .46-.18.49-.42l.38-2.65c.61-.25 1.17-.59 1.69-.98l2.49 1c.23.09.49 0 .61-.22l2-3.46c.12-.22.07-.49-.12-.64l-2.11-1.65zM12 15.5c-1.93 0-3.5-1.57-3.5-3.5s1.57-3.5 3.5-3.5 3.5 1.57 3.5 3.5-1.57 3.5-3.5 3.5z\"/>
</svg>
"
                fill width height))

   :info (fn [{:keys [width height fill]}]
           (util/format
            "<svg fill=\"%s\" xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" viewBox=\"0 0 24 24\">
    <path d=\"M0 0h24v24H0z\" fill=\"none\"/>
    <path d=\"M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z\"/>
</svg>
"
            fill width height))

   :check_circle (fn [{:keys [width height fill]}]
                   (util/format
                    "<svg fill=\"%s\" width=\"%d\" height=\"%d\" xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 24 24\">
    <path d=\"M0 0h24v24H0z\" fill=\"none\"/>
    <path d=\"M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z\"/>
</svg>
"
                    fill width height))

   :error (fn [{:keys [width height fill]}]
            (util/format
             "<svg fill=\"%s\" width=\"%d\" height=\"%d\" xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 24 24\">
    <path d=\"M0 0h24v24H0z\" fill=\"none\"/>
    <path d=\"M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z\"/>
</svg>
"
             fill width height))
   :warning (fn [{:keys [width height fill]}]
              (util/format
               "<svg fill=\"%s\" width=\"%d\" height=\"%d\" xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 24 24\">
    <path d=\"M0 0h24v24H0z\" fill=\"none\"/>
    <path d=\"M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z\"/>
</svg>
"
               fill width height))

   :translate (fn [{:keys [width height fill]}]
                (util/format
                 "<svg fill=\"%s\" width=\"%d\" height=\"%d\" xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 24 24\">
    <path d=\"M0 0h24v24H0z\" fill=\"none\"/>
    <path d=\"M12.87 15.07l-2.54-2.51.03-.03c1.74-1.94 2.98-4.17 3.71-6.53H17V4h-7V2H8v2H1v1.99h11.17C11.5 7.92 10.44 9.75 9 11.35 8.07 10.32 7.3 9.19 6.69 8h-2c.73 1.63 1.73 3.17 2.98 4.56l-5.09 5.02L4 19l5-5 3.11 3.11.76-2.04zM18.5 10h-2L12 22h2l1.12-3h4.75L21 22h2l-4.5-12zm-2.62 7l1.62-4.33L19.12 17h-3.24z\"/>
</svg>
"
                 fill width height))

   :twitter (fn [{:keys [width height fill]}]
              (util/format
               "<svg fill=\"%s\" width=\"%d\" height=\"%d\" xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 128 128\"><g data-width=\"128\" data-height=\"104\" display=\"inline\" transform=\"translate(0 12)\"><path d=\"M40.255 104c-14.83 0-28.634-4.346-40.255-11.796 2.054.242 4.145.366 6.264.366 12.303 0 23.627-4.197 32.614-11.239-11.491-.212-21.19-7.803-24.531-18.234 1.603.307 3.248.471 4.94.471 2.395 0 4.715-.321 6.919-.921-12.014-2.412-21.065-13.023-21.065-25.744 0-.111 0-.221.002-.33 3.541 1.966 7.59 3.147 11.895 3.284-7.046-4.708-11.683-12.744-11.683-21.853 0-4.811 1.295-9.322 3.555-13.199 12.952 15.884 32.302 26.337 54.128 27.432-.448-1.921-.68-3.925-.68-5.983 0-14.499 11.758-26.254 26.262-26.254 7.553 0 14.378 3.189 19.168 8.291 5.982-1.178 11.602-3.363 16.676-6.371-1.961 6.131-6.125 11.276-11.547 14.525 5.312-.635 10.373-2.046 15.083-4.134-3.521 5.265-7.973 9.889-13.104 13.591.051 1.126.076 2.258.076 3.397 0 34.695-26.414 74.701-74.717 74.701\"></path></g></svg>"
               fill width height))
   :rss (fn [{:keys [width height fill]}]
          (util/format
           "<svg fill=\"%s\" width=\"%d\" height=\"%d\" xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 24 24\">
    <path fill=\"none\" d=\"M0 0h24v24H0z\"/>
    <circle cx=\"6.18\" cy=\"17.82\" r=\"2.18\"/>
    <path d=\"M4 4.44v2.83c7.03 0 12.73 5.7 12.73 12.73h2.83c0-8.59-6.97-15.56-15.56-15.56zm0 5.66v2.83c3.9 0 7.07 3.17 7.07 7.07h2.83c0-5.47-4.43-9.9-9.9-9.9z\"/>
</svg>
"
           fill width height))

   :drafts (fn [{:keys [width height fill]}]
             (util/format
              "<svg xmlns=\"http://www.w3.org/2000/svg\"  fill=\"%s\" width=\"%d\" height=\"%d\"  viewBox=\"0 0 24 24\">
    <path d=\"M21.99 8c0-.72-.37-1.35-.94-1.7L12 1 2.95 6.3C2.38 6.65 2 7.28 2 8v10c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2l-.01-10zM12 13L3.74 7.84 12 3l8.26 4.84L12 13z\"/>
    <path d=\"M0 0h24v24H0z\" fill=\"none\"/>
</svg>
"
              fill width height))

   ;; right-up
   :connect (fn [{:keys [width height fill]}]
              (util/format
               "<svg xmlns=\"http://www.w3.org/2000/svg\" fill=\"%s\" width=\"%d\" height=\"%d\" viewBox=\"0 0 48 48\">
    <path d=\"M0 0h48v48H0z\" fill=\"none\"/>
    <path d=\"M18 10v4h13.17L8 37.17 10.83 40 34 16.83V30h4V10z\"/>
</svg>

"
               fill width height))

   :left-down (fn [{:keys [width height fill]}]
                (util/format
                 "<svg xmlns=\"http://www.w3.org/2000/svg\" fill=\"%s\" width=\"%d\" height=\"%d\" viewBox=\"0 0 24 24\">
    <path d=\"M0 0h24v24H0z\" fill=\"none\"/>
    <path d=\"M20 5.41L18.59 4 7 15.59V9H5v10h10v-2H8.41z\"/>
</svg>
"
                 fill width height))

   :rectangle (fn [{:keys [width height fill]}]
                (util/format
                 "<svg xmlns=\"http://www.w3.org/2000/svg\" fill=\"%s\" width=\"%d\" height=\"%d\" viewBox=\"0 0 24 24\">
    <path d=\"M0 0h24v24H0z\" fill=\"none\"/>
    <path d=\"M19 5H5c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 12H5V7h14v10z\"/>
</svg>
"
                 fill width height))

   :t (fn [{:keys [width height fill]}]
        (util/format
         "<svg class=\"svg\" fill=\"%s\" width=\"%d\" height=\"%d\" xmlns=\"http://www.w3.org/2000/svg\"><path d=\"M2 5h1V2h5v14H5v1h7v-1H9V2h5v3h1V1H2v4z\"></path></svg>"
         fill width height))

   :file-download (fn [{:keys [width height fill]}]
                    (util/format
                     "<svg xmlns=\"http://www.w3.org/2000/svg\" fill=\"%s\" width=\"%d\" height=\"%d\" viewBox=\"0 0 24 24\">
    <path d=\"M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z\"/>
    <path d=\"M0 0h24v24H0z\" fill=\"none\"/>
</svg>
"
                     fill width height))

   :more (fn [{:keys [width height fill]}]
           (util/format
            "<svg xmlns=\"http://www.w3.org/2000/svg\" fill=\"%s\" width=\"%d\" height=\"%d\" viewBox=\"0 0 24 24\">
    <path d=\"M0 0h24v24H0z\" fill=\"none\"/>
    <path d=\"M6 10c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm12 0c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm-6 0c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z\"/>
</svg>
"
            fill width height))

   })
