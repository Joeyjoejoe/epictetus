(ns epictetus.core
  (:require [integrant.core :as ig]
            [clojure.java.io :as io]
            [epictetus.window :as _]
            [epictetus.program :as __]))

(defn start
  ([] (start "engine-default.edn"))
  ([path]
   (let [config (-> path io/resource slurp ig/read-string)]
     (ig/init config))))
