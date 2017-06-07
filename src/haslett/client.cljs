(ns haslett.client
  (:require [cljs.core.async :as a :refer [<! >!]]
            [cljs.reader :as edn])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defn websocket
  ([url]
   (websocket url {}))
  ([url options]
   (let [source (:source options (a/chan 1 (map edn/read-string)))
         sink   (:sink options   (a/chan 1 (map pr-str)))
         socket (js/WebSocket. url)
         return (a/promise-chan)]
     (aset socket "onopen"    (fn [_] (a/put! return {:source source, :sink sink})))
     (aset socket "onclose"   (fn [_] (a/close! source) (a/close! sink)))
     (aset socket "onmessage" (fn [e] (a/put! source (.-data e))))
     (go-loop []
       (when-let [msg (<! sink)]
         (.send socket msg)
         (recur)))
     return)))
