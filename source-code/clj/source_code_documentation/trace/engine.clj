
(ns source-code-documentation.trace.engine
    (:require [source-code-documentation.trace.utils :as trace.utils]
              [fruits.vector.api :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn trace-redirection
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) declaration
  ; @param (map) header-block
  ;
  ; @example
  ; (trace-header-redirection [...] {...} {...}
  ;                           {:name "my-function" :header [{:type :redirect :meta ["..."] :indent 1}]}
  ;                           {:type :redirect :meta ["..."] :indent 1})
  ; =>
  ; {:type :redirect :meta ["..."] :indent 1 :pointer "another-namespace/another-function"}
  ;
  ; @return (map)
  [_ _ file-data declaration header-block]
  (let [pointer (trace.utils/derive-pointer                 file-data declaration header-block)
        pointer (trace.utils/invoke-pointer-namespace-alias file-data declaration pointer)]
       (assoc header-block :pointer pointer)))

(defn trace-link
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) declaration
  ; @param (map) header-block
  ;
  ; @return (map)
  [state options file-data declaration header-block]
  ; Tracing links is the same process as tracing redirections.
  (trace-redirection state options file-data declaration header-block))

(defn trace-header
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) declaration
  ;
  ; @example
  ; (trace-header [...] {...} {...}
  ;               {:name "my-function" :header [{:type :redirect :meta ["..."] :indent 1}]})
  ; =>
  ; {:name   "my-function"
  ;  :header [{:type :redirect :meta ["..."] :indent 1 :pointer "another-namespace/another-function"}]}
  ;
  ; @return (map)
  [state options file-data declaration]
  (letfn [(f0 [%] (-> % :type (= :redirect)))
          (f1 [%] (-> % :type (= :link)))
          (f2 [%] (trace-redirection state options file-data declaration %))
          (f3 [%] (trace-link        state options file-data declaration %))]
         (-> declaration (update :header vector/->items-by f0 f2)
                         (update :header vector/->items-by f1 f3))))

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
  [state options file-data]
  (letfn [(f0 [%] (trace-header state options file-data %))]
         (update file-data :declarations vector/->items f0)))

(defn trace-imported-files
  ; @ignore
  ;
  ; @description
  ; - Traces the links and redirections in headers of defs and defns creating pointers (for all source files within the given source directories).
  ; - Although the documentation generator creates documentation only for files that match the provided (or default)
  ;   filename pattern, to handle header links and redirections, it requires tracing them for all available source files.
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ;
  ; @return (maps in vector)
  [state options]
  (letfn [(f0 [%] (trace-imported-file state options %))]
         (vector/->items state f0)))
