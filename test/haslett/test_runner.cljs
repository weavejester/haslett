(ns haslett.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [haslett.client-test]))

(doo-tests 'haslett.client-test)
