
(ns source-code-documentation.read.engine
    (:require [map.api :as map]
              [source-code-documentation.read.utils :as read.utils]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-imported-file
  [filepath file-data]
  (-> file-data (update :headers map/->values read.utils/read-header)))

(defn read-imported-files
  ; @ignore
  ;
  ; @param (map) state
  ; @param (map) options
  ; {}
  ;
  ; @return (map)
  [state _]
  (letfn [(f0 [filepath file-data] (if (-> file-data :create-documentation?)
                                       (-> filepath (read-imported-file file-data))))]
  ;                                     (-> file-data)))]
         (map/->values state f0 {:provide-key? true})))


  ;(letfn [(f0 [filename] (regex/re-match? filename filename-pattern))]
  ;       (-> state (map/copy :ns-maps :defs :ns-maps :defns)
  ;                 (update   :defs  map/keep-keys-by f0)
  ;                 (update   :defns map/keep-keys-by f0)
  ;                 (update   :defs  map/->values import-ns-defs  {:provide-key? true})
  ;                 (update   :defns map/->values import-ns-defns {:provide-key? true})

  ;                 (dissoc :ns-maps)])
