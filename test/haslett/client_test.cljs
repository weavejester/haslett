(ns haslett.client-test
  (:require [cljs.test :refer-macros [deftest is testing async]]
            [cljs.core.async :as a :refer [<! >!]]
            [cljs.core.async.impl.protocols :as ap]
            [haslett.client :as ws]
            [haslett.format :as fmt])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(deftest test-defaults
  (async done
    (go (let [stream (<! (ws/connect "ws://localhost:3200"))]
          (is (ws/connected? stream))
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
          (is (= (<! (ws/close stream))      {:code 1000, :reason "Closed by creator"}))
          (is (= (<! (:close-status stream)) {:code 1000, :reason "Closed by creator"}))
          (done)))))

(deftest test-connection-fail
  (async done
    (go (let [stream (<! (ws/connect "ws://localhost:3201"))]
          (is (not (ws/connected? stream)))
          (is (ap/closed? (:sink stream)))
          (is (ap/closed? (:source stream)))
          (is (= (<! (:close-status stream)) {:code 1006, :reason ""}))
          (done)))))

(deftest test-local-close
  (async done
    (go (let [stream (<! (ws/connect "ws://localhost:3200"))]
          (a/close! (:sink stream))
          (is (= (<! (:close-status stream)) {:code 1000, :reason "Closed by creator"}))
          (is (ap/closed? (:source stream)))
          (done)))))

(deftest test-chans-not-closed
  (async done
    (go (let [stream (<! (ws/connect "ws://localhost:3200" {:close-chan? false}))]
          (ws/close stream)
          (is (= (<! (:close-status stream)) {:code 1000, :reason "Closed by creator"}))
          (is (not (ap/closed? (:sink stream))))
          (is (not (ap/closed? (:source stream))))
          (done)))))
