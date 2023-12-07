
(ns source-code-documentation.read.engine
    (:require [map.api :as map]
              [source-code-documentation.read.utils :as read.utils]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-imported-file
  ; @ignore
  ;
  ; @param (string) filepath
  ; @param (map) file-data
  ;
  ; @return (map)
  [filepath file-data]
  (-> file-data (update :headers map/->values read.utils/read-header)))

(defn read-imported-files
  ; @ignore
  ;
  ; @param (map) state
  ; @param (map) options
  ;
  ; @return (map)
  [state _]
  (letfn [(f0 [filepath file-data] (if (-> file-data :create-documentation?)
                                       (-> filepath (read-imported-file file-data))
                                       (-> file-data)))]
         (map/->values state f0 {:provide-key? true})))
