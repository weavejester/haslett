(ns haslett.client
  (:require [cljs.core.async :as a :refer [<! >!]]
            [cljs.reader :as edn])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defn- chan-fn [xf]
  (fn chan
    ([]                          (chan 1))
    ([buf-or-n]                  (chan buf-or-n identity))
    ([buf-or-n xform]            (chan buf-or-n xform nil))
    ([buf-or-n xform ex-handler] (a/chan buf-or-n (comp xf xform) ex-handler))))

(def edn-source  (chan-fn (map edn/read-string)))
(def edn-sink    (chan-fn (map pr-str)))

(def json-source (chan-fn (map #(js->clj (js/JSON.parse %)))))
(def json-sink   (chan-fn (map #(js/JSON.stringify (clj->js %)))))

(defn websocket
  ([url]
   (websocket url {}))
  ([url options]
   (let [source (:source options (edn-source))
         sink   (:sink options   (edn-sink))
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
