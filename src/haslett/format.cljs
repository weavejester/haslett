(ns haslett.format
  (:refer-clojure :exclude [identity])
  (:require [cljs.reader :as edn]
            [cognitect.transit :as transit]))

(defprotocol Format
  (read  [formatter string])
  (write [formatter value]))

(def identity
  (reify Format
    (read  [_ s] s)
    (write [_ v] v)))

(def transit
  (reify Format
    (read  [_ s] (transit/read (transit/reader :json) s))
    (write [_ v] (transit/write (transit/writer :json) v))))

(def edn
  (reify Format
    (read  [_ s] (edn/read-string s))
    (write [_ v] (pr-str v))))

(def json
  (reify Format
    (read  [_ s] (js->clj (js/JSON.parse s)))
    (write [_ v] (js/JSON.stringify (clj->js v)))))
