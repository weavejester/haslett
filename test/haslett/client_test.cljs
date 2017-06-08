(ns haslett.client-test
  (:require [cljs.test :refer-macros [deftest is testing async]]
            [cljs.core.async :as a :refer [<! >!]]
            [haslett.client :as ws]
            [haslett.format :as fmt])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(deftest test-defaults
  (async done
    (go (let [stream (<! (ws/connect "ws://localhost:3200"))]
          (>! (:sink stream) "Hello World")
          (is (= (<! (:source stream)) "Hello World"))
          (ws/close stream)
          (done))))) 

(deftest test-transit
  (async done
    (go (let [stream (<! (ws/connect "ws://localhost:3200" {:format fmt/transit}))]
          (>! (:sink stream) {:hello "World"})
          (is (= (<! (:source stream)) {:hello "World"}))
          (ws/close stream)
          (done)))))

(deftest test-edn
  (async done
    (go (let [stream (<! (ws/connect "ws://localhost:3200" {:format fmt/edn}))]
          (>! (:sink stream) {:hello "World"})
          (is (= (<! (:source stream)) {:hello "World"}))
          (ws/close stream)
          (done)))))

(deftest test-json
  (async done
    (go (let [stream (<! (ws/connect "ws://localhost:3200" {:format fmt/json}))]
          (>! (:sink stream) {:hello "World"})
          (is (= (<! (:source stream)) {"hello" "World"}))
          (ws/close stream)
          (done)))))

(deftest test-close
  (async done
    (go (let [stream (<! (ws/connect "ws://localhost:3200"))]
          (is (= (<! (ws/close stream))      {:code 1005, :reason ""}))
          (is (= (<! (:close-status stream)) {:code 1005, :reason ""}))
          (done)))))
