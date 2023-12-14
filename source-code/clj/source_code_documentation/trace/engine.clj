
(ns source-code-documentation.trace.engine
    (:require [source-code-documentation.trace.utils :as trace.utils]
              [fruits.vector.api :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn trace-header-redirection
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) header
  ; @param (map) header-block
  ;
  ; @example
  ; (trace-header-redirection [...] {...} {...}
  ;                           {:name "my-function" :blocks [{:type :redirect :meta ["..."] :indent 1}]}
  ;                           {:type :redirect :meta ["..."] :indent 1})
  ; =>
  ; {:type :redirect :meta ["..."] :indent 1 :pointer "another-namespace/another-function"}
  ;
  ; @return (map)
  [_ _ file-data header header-block]
  (let [pointer (trace.utils/derive-pointer                 file-data header header-block)
        pointer (trace.utils/invoke-pointer-namespace-alias file-data header pointer)]
       (assoc header-block :pointer pointer)))

(defn trace-header-link
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) header
  ; @param (map) header-block
  ;
  ; @return (map)
  [state options file-data header header-block]
  ; Tracing header links is the same process as tracing header redirections.
  (trace-header-redirection state options file-data header header-block))

(defn trace-header
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) header
  ;
  ; @example
  ; (trace-header [...] {...} {...}
  ;               {:name "my-function" :blocks [{:type :redirect :meta ["..."] :indent 1}]})
  ; =>
  ; [{:type :redirect :meta ["..."] :indent 1 :pointer "another-namespace/another-function"}]
  ;
  ; @return (maps in vector)
  [state options file-data header]
  (letfn [(f0 [%] (-> % :type (= :redirect)))
          (f1 [%] (-> % :type (= :link)))
          (f2 [%] (trace-header-redirection state options file-data header %))
          (f3 [%] (trace-header-link        state options file-data header %))]
         (-> header (update :blocks vector/->items-by f0 f2)
                    (update :blocks vector/->items-by f1 f3))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn trace-imported-file
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (maps in vector)
  [state options {:keys [headers] :as file-data}]
  (letfn [(f0 [%] (trace-header state options file-data %))]
         (update file-data :headers vector/->items f0)))

(defn trace-imported-files
  ; @ignore
  ;
  ; @description
  ; - Traces the read headers of defs and defns creating pointers for header links and redirections (for all source files within the given source directories).
  ; - Although the documentation generator creates documentation only for files that match the provided (or default)
  ;   filename pattern, to handle header links and redirections, it requires tracing headers for all available source files.
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ;
  ; @return (maps in vector)
  [state options]
  (letfn [(f0 [%] (trace-imported-file state options %))]
         (vector/->items state f0)))
