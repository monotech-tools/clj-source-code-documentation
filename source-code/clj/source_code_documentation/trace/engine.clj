
(ns source-code-documentation.trace.engine
    (:require [source-code-documentation.trace.utils :as trace.utils]
              [fruits.vector.api :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn trace-declaration-header-redirection
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (string) declaration-name
  ; @param (map) declaration-header-block
  ;
  ; @return (map)
  [_ _ file-data declaration-name declaration-header-block]
  (let [pointer (trace.utils/derive-pointer                 file-data declaration-name declaration-header-block)
        pointer (trace.utils/invoke-pointer-namespace-alias file-data declaration-name pointer)]
       (assoc declaration-header-block :pointer pointer)))

(defn trace-declaration-header
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (string) declaration-name
  ; @param (maps in vector) declaration-header
  ;
  ; @example
  ; (trace-declaration-header [...] {...} {...}
  ;                           "my-function"
  ;                           [{:type :redirect :meta ["..."] :indent 1}])
  ; =>
  ; [{:type :redirect :meta ["..."] :indent 1}]
  ;
  ; @return (maps in vector)
  [state options file-data declaration-name declaration-header]
  (letfn [(f0 [%] (-> % :type (= :redirect)))
          (f1 [%] (trace-declaration-header-redirection state options file-data declaration-name %))]
         (vector/->items-by declaration-header f0 f1)))

(defn trace-imported-file
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (maps in vector)
  [state options {:keys [headers] :as file-data}]
  (letfn [(f0 [declaration-name declaration-header] (trace-declaration-header state options file-data declaration-name declaration-header))]
         (update file-data :headers map/->values f0 {:provide-key? true})))

(defn trace-imported-files
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ;
  ; @return (maps in vector)
  [state options]
  (letfn [(f0 [file-data] (trace-imported-file state options file-data))]
         (vector/->items state f0)))
