
(ns source-code-documentation.core.engine
    (:require [source-code-documentation.core.prototypes :as core.prototypes]
              [source-code-documentation.core.tests      :as core.tests]
              [source-code-documentation.import.engine   :as import.engine]
              [source-code-documentation.map.engine      :as map.engine]
              [source-code-documentation.print.engine    :as print.engine]
              [source-code-documentation.read.engine     :as read.engine]
              [source-code-documentation.resolve.engine  :as resolve.engine]
              [source-code-documentation.trace.engine    :as trace.engine]
              [validator.api                             :as v]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn generate-documentation!
  ; @important
  ; This function ereases the output directory before exporting the documentation files!
  ;
  ; @description
  ; Reads source files from the given source paths that match the given filename pattern,
  ; and generates a HTML documentation book to the given output path.
  ;
  ; @param (map) options
  ; {:author (map)(opt)
  ;   {:name (string)(opt)
  ;    :website (string)(opt)}
  ;  :base-uri (string)
  ;  :declaration-order (keywords in vector)
  ;   Order of content blocks within declaration sections (by type).
  ;  :filename-pattern (regex pattern)(opt)
  ;   Default: #"[a-z\_\d]+\.clj[cs]?"
  ;  :library (map)(opt)}
  ;   {:name (string)(opt)
  ;    :version (string)(opt)
  ;    :website (string)(opt)}
  ;  :output-path (string)
  ;  :previews-uri (string)(opt)
  ;  :source-paths (strings in vector)
  ;  :tutorial-order (keywords in vector)
  ;   Order of content blocks within tutorial sections (by type).}
  ;
  ; @usage
  ; (generate-documentation! {...})
  ;
  ; @usage
  ; (generate-documentation! {:author            {:name "Author" :website "https://author.com"}
  ;                           :declaration-order [:source-code :description :usage ...]
  ;                           :filename-pattern  "[a-z\_]\.clj"
  ;                           :library           {:name "my-library" :version "1.0.0" :website "https://github.com/author/my-library"}
  ;                           :output-path       "docs"
  ;                           :previews-uri      "https://github.com/author/my-library/blob/main/previews"
  ;                           :source-paths      ["source-code"]})
  [options]
  (if (v/valid? options core.tests/OPTIONS-TEST {:prefix "options"})
      (let [options (core.prototypes/options-prototype options)]
           (-> [] (map.engine/map-source-paths           options)
                  (import.engine/import-source-files     options)
                  (read.engine/read-imported-files       options)
                  (trace.engine/trace-imported-files     options)
                  (resolve.engine/resolve-imported-files options)
                  (print.engine/print-documentation!     options)))))
