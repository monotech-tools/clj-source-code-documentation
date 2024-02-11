
(ns source-code-documentation.import.utils
    (:require [fruits.normalize.api :as normalize]
              [fruits.regex.api     :as regex]
              [fruits.string.api    :as string]
              [fruits.vector.api    :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn first-coherent-comment-row-group
  ; @ignore
  ;
  ; @param (string) n
  ;
  ; @usage
  ; (first-coherent-comment-row-group "\n ; Commented row #1\n Non-commented row\n ; Commented row #2\n ; Commented row #3")
  ; =>
  ; [" ; Commented row #1"]
  ;
  ; @return (strings in vector)
  [n]
  (letfn [(f0 [       %] (regex/re-match? % #"^[\h]*\;"))  ; <- Returns TRUE if the given value is a comment row.
          (f1 [       %] (regex/re-match? % #"\n[\h]*\;")) ; <- Returns TRUE if the given value contains any comment rows.
          (f2 [result %] (conj result (string/trim %)))]   ; <- Trims the given value (comment row) then appends it to the result vector.
         (loop [observed-part n result []]
               (let [row-ends-at (or (string/first-dex-of observed-part "\n") (count observed-part))
                     row-content (string/keep-range observed-part 0 (->  row-ends-at))
                     rest-part   (string/keep-range observed-part   (inc row-ends-at))]
                    (if (-> result empty?)
                        (cond (-> observed-part string/empty?) (-> result)
                              (-> row-content f0)              (-> rest-part (recur (f2 result row-content)))
                              :no-comment-row-found-yet        (-> rest-part (recur result)))
                        (cond (-> observed-part string/empty?) (-> result)
                              (-> row-content f0)              (-> rest-part (recur (f2 result row-content)))
                              :first-coherent-group-ended      (-> result)))))))

(defn last-coherent-comment-row-group
  ; @ignore
  ;
  ; @param (string) n
  ;
  ; @usage
  ; (last-coherent-comment-row-group "\n ; Commented row #1\n Non-commented row\n ; Commented row #2\n ; Commented row #3")
  ; =>
  ; [" ; Commented row #2"
  ;  " ; Commented row #3"]
  ;
  ; @return (strings in vector)
  [n]
  (letfn [(f0 [       %] (regex/re-match? % #"^[\h]*\;"))  ; <- Returns TRUE if the given value is a comment row.
          (f1 [       %] (regex/re-match? % #"\n[\h]*\;")) ; <- Returns TRUE if the given value contains any comment rows.
          (f2 [result %] (conj result (string/trim %)))]   ; <- Trims the given value (comment row) then appends it to the result vector.
         (loop [observed-part n result []]
               (let [row-ends-at (or (string/first-dex-of observed-part "\n") (count observed-part))
                     row-content (string/keep-range observed-part 0 (->  row-ends-at))
                     rest-part   (string/keep-range observed-part   (inc row-ends-at))]
                    (cond (-> observed-part string/empty?) (-> result)
                          (-> row-content f0)              (-> rest-part (recur (f2 result row-content)))
                          (-> rest-part   f1)              (-> rest-part (recur []))
                          :no-further-comment-rows         (-> rest-part (recur result)))))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn import-def-value
  ; @ignore
  ;
  ; @description
  ; Imports the value of a specific def declaration.
  ;
  ; @param (map) file-data
  ; @param (string) file-content
  ; @param (map) def
  ;
  ; @usage
  ; (import-def-value "... (def my-constant :my-value) ..." {...})
  ; =>
  ; ":my-value"
  ;
  ; @return (string)
  [_ file-content {:keys [value]}]
  (string/keep-range file-content (-> value :bounds first)
                                  (-> value :bounds second)))

(defn import-def-header
  ; @ignore
  ;
  ; @description
  ; Imports the header (documentation content) of a specific def declaration.
  ;
  ; @param (map) file-data
  ; @param (string) file-content
  ; @param (map) def
  ;
  ; @usage
  ; (import-def-header "... \n; @constant (keyword)\n(def my-constant :my-value) ..." {...})
  ; =>
  ; "; @constant (keyword)"
  ;
  ; @return (strings in vector)
  [_ file-content {:keys [bounds]}]
  (-> file-content (string/keep-range 0 (first bounds))                   ; <- Cuts off the rest of the file content from the start position of the def.
                   (string/trim-end)                                      ; <- Removes the indent (if any) that precedes the def.
                   (regex/after-last-match #"\n[\h]*\n" {:return? false}) ; <- Keeps the part after the last empty row.
                   (last-coherent-comment-row-group)))                    ; <- Extracts the last coherent comment row group.

(defn import-defn-header
  ; @ignore
  ;
  ; @description
  ; Imports the header (documentation content) of a specific defn declaration.
  ;
  ; @param (map) file-data
  ; @param (string) file-content
  ; @param (map) defn
  ;
  ; @usage
  ; (import-defn-header "... (defn my-function\n; @param (map) my-param\n [my-param] ...) ..." {...})
  ; =>
  ; "; @param (map) my-param"
  ;
  ; @return (strings in vector)
  [_ file-content {:keys [bounds]}]
  (-> file-content (string/keep-range (first bounds) (last bounds))    ; <- Keeps the part of the file content from the start position of the defn - to its end position.
                   (regex/before-first-match #"\n[\h]+\[|\n[\h]+\(\[") ; <- Cuts off the part from the first (non-commented and non-quoted) argument list.
                   (last-coherent-comment-row-group)))                 ; <- Extracts the last coherent comment row group.

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn import-def-source-code
  ; @ignore
  ;
  ; @description
  ; Returns the source code of a specific def declaration.
  ;
  ; @param (map) file-data
  ; @param (string) file-content
  ; @param (map) def
  ;
  ; @usage
  ; (import-def-source-code "... (def my-constant [] ...) ..." {...})
  ; =>
  ; "(def my-constant :my-value)"
  ;
  ; @return (string)
  [_ file-content {:keys [bounds]}]
  (string/keep-range file-content (first bounds) (last bounds)))

(defn import-defn-source-code
  ; @ignore
  ;
  ; @description
  ; Returns the source code of a specific defn declaration.
  ;
  ; @param (map) file-data
  ; @param (string) file-content
  ; @param (map) defn
  ;
  ; @usage
  ; (import-defn-source-code "... (defn my-function [] ...) ..." {...})
  ; =>
  ; "(defn my-function [] ...)"
  ;
  ; @return (string)
  [_ file-content {:keys [bounds]}]
  (string/keep-range file-content (first bounds) (last bounds)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn import-def
  ; @ignore
  ;
  ; @param (map) file-data
  ; @param (string) file-content
  ; @param (map) def
  ;
  ; @return (maps in vector)
  [file-data file-content def]
  {:source-code (import-def-source-code file-data file-content def)
   :content     (import-def-header      file-data file-content def)
   :label       (-> def :name)
   :name        (-> def :name)
   :type        (->     :def)})

(defn import-defn
  ; @ignore
  ;
  ; @param (map) file-data
  ; @param (string) file-content
  ; @param (map) defn
  ;
  ; @return (maps in vector)
  [file-data file-content defn]
  {:source-code (import-defn-source-code file-data file-content defn)
   :content     (import-defn-header      file-data file-content defn)
   :label       (-> defn :name)
   :name        (-> defn :name)
   :type        (->      :defn)})

(defn import-tutorial
  ; @ignore
  ;
  ; @param (map) file-data
  ; @param (string) file-content
  ; @param (string) substring
  ;
  ; @return (maps in vector)
  [_ _ substring]
  (letfn [(f0 [%] (regex/re-first % #"(?<=\@tutorial[\h]+)[^\n]+(?=\n)"))
          (f1 [%] (string/after-first-occurence % "\n"))]
         {:content (-> substring f1 (first-coherent-comment-row-group))
          :label   (-> substring f0)
          :name    (-> substring f0 normalize/clean-text)
          :type    (-> :tutorial)}))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn import-def-values
  ; @ignore
  ;
  ; @description
  ; Imports symbol type values of defs (values are important for creating redirection traces).
  ;
  ; @param (map) file-data
  ; @param (string) file-content
  ;
  ; @return (maps in vector)
  [{{:keys [defs]} :ns-map :as file-data} file-content]
  (letfn [(f0 [%] (import-def-value file-data file-content %))
          (f1 [%] (-> % :value :type (= :symbol)))
          (f2 [%] (-> % (assoc-in [:value :symbol] (f0 %))))]
         (vector/update-items-by defs f1 f2)))

(defn import-defs
  ; @ignore
  ;
  ; @param (map) file-data
  ; @param (string) file-content
  ;
  ; @return (maps in vector)
  [{{:keys [defs]} :ns-map :as file-data} file-content]
  (letfn [(f0 [%] (import-def file-data file-content %))]
         (vector/->items defs f0)))

(defn import-defns
  ; @ignore
  ;
  ; @param (map) file-data
  ; @param (string) file-content
  ;
  ; @return (maps in vector)
  [{{:keys [defns]} :ns-map :as file-data} file-content]
  (letfn [(f0 [%] (import-defn file-data file-content %))]
         (vector/->items defns f0)))

(defn import-tutorials
  ; @ignore
  ;
  ; @param (map) file-data
  ; @param (string) file-content
  ;
  ; @return (maps in vector)
  [file-data file-content]
  ; - The newline character could be in a positive lookbehind assertion within the regex pattern.
  ;   But unfortunatelly, the 'regex/first-dex-of' function would return incorrect positions.
  ; - The 'f1' function cuts off the part after the second match before passing the substring to
  ;   the 'first-coherent-comment-row-group' function, to prevent reading multiple tutorials
  ;   from one comment row group (if they are joined).
  (letfn [(f0 [%] (regex/first-dex-of       % #"\n[\h]*\;[\h]*\@tutorial"))
          (f1 [%] (regex/before-first-match % #"\n[\h]*\;[\h]*\@tutorial" {:return? true}))
          (f2 [%] (import-tutorial file-data file-content %))]
         (loop [substring file-content tutorials []]
               (if-let [position (f0 substring)]
                       (let [substring (string/keep-range substring (inc position))]
                            (recur substring (vector/conj-item tutorials (-> substring f1 f2))))
                       (-> tutorials)))))
