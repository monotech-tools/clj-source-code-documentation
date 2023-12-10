
(ns source-code-documentation.trace.engine
    (:require [source-code-documentation.trace.utils :as trace.utils]
              [fruits.vector.api :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn trace-def*-header-redirection
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (string) def*-name
  ; @param (map) def*-header-block
  ;
  ; @return (map)
  [_ _ file-data def*-name def*-header-block]
  (let [pointer (trace.utils/derive-pointer                 file-data def*-name def*-header-block)
        pointer (trace.utils/invoke-pointer-namespace-alias file-data def*-name pointer)]
       (assoc def*-header-block :pointer pointer)))

(defn trace-def*-header
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (string) def*-name
  ; @param (maps in vector) def*-header
  ;
  ; @example
  ; (trace-def*-header [...] {...} {...}
  ;                    "my-function"
  ;                    [{:type :redirect :meta ["..."] :indent 1}])
  ; =>
  ; [{:type :redirect :meta ["..."] :indent 1}]
  ;
  ; @return (maps in vector)
  [state options file-data def*-name def*-header]
  (letfn [(f0 [%] (-> % :type (= :redirect)))
          (f1 [%] (trace-def*-header-redirection state options file-data def*-name %))]
         (vector/->items-by def*-header f0 f1)))

(defn trace-imported-file
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (maps in vector)
  [state options {:keys [headers] :as file-data}]
  (letfn [(f0 [def*-name def*-header] (trace-def*-header state options file-data def*-name def*-header))]
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
