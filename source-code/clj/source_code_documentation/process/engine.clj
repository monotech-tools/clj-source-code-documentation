
(ns source-code-documentation.process.engine
    (:require [fruits.map.api :as map]
              [source-code-documentation.process.utils :as process.utils]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn process-read-file
  ; @ignore
  ;
  ; @param (string) filepath
  ; @param (map) file-data
  ;
  ; @return (map)
  [filepath file-data]
  (-> file-data (update :headers map/->values process.utils/process-header)))

(defn process-read-files
  ; @ignore
  ;
  ; @param (map) state
  ; @param (map) options
  ;
  ; @return (map)
  [state _]
  (letfn [(f0 [filepath file-data] (if (-> file-data :create-documentation?)
                                       (-> filepath (process-read-file file-data))
                                       (-> file-data)))]
         (map/->values state f0 {:provide-key? true})))
