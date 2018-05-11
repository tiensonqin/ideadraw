(ns share.routes)

(def routes
  ["/" [[""                                                       :home]
        [["@" :screen_name]                                       :user]
        [[:file]                                                  :file]]])
