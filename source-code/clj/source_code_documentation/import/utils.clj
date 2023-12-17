
(ns source-code-documentation.import.utils
    (:require [fruits.regex.api  :as regex]
              [fruits.string.api :as string]
              [fruits.vector.api :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn first-coherent-comment-row-group
  ; @ignore
  ;
  ; @param (string) n
  ;
  ; @example
  ; (first-coherent-comment-row-group "\n ; Commented row #1\n Non-commented row\n ; Commented row #2\n ; Commented row #3")
  ; =>
  ; [" ; Commented row #1"]
  ;
  ; @return (strings in vector)
  [n]
  (letfn [(f0 [       %] (regex/re-match? % #"^[\h]{0,}\;"))  ; <- Returns TRUE if the given value is a comment row.
          (f1 [       %] (regex/re-match? % #"\n[\h]{0,}\;")) ; <- Returns TRUE if the given value contains any comment rows.
          (f2 [result %] (conj result (string/trim %)))]      ; <- Trims the given value (comment row) then appends it to the result vector.
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
  ; @example
  ; (last-coherent-comment-row-group "\n ; Commented row #1\n Non-commented row\n ; Commented row #2\n ; Commented row #3")
  ; =>
  ; [" ; Commented row #2"
  ;  " ; Commented row #3"]
  ;
  ; @return (strings in vector)
  [n]
  (letfn [(f0 [       %] (regex/re-match? % #"^[\h]{0,}\;"))  ; <- Returns TRUE if the given value is a comment row.
          (f1 [       %] (regex/re-match? % #"\n[\h]{0,}\;")) ; <- Returns TRUE if the given value contains any comment rows.
          (f2 [result %] (conj result (string/trim %)))]      ; <- Trims the given value (comment row) then appends it to the result vector.
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
  ; Returns the value of a specific def declaration.
  ;
  ; @param (string) file-content
  ; @param (map) def
  ;
  ; @example
  ; (import-def-value "... (def my-constant :my-value) ..." {...})
  ; =>
  ; ":my-value"
  ;
  ; @return (string)
  [file-content {:keys [value]}]
  (string/keep-range file-content (-> value :bounds first)
                                  (-> value :bounds second)))

(defn import-def-header
  ; @ignore
  ;
  ; @description
  ; Returns the comment header of a specific def declaration.
  ;
  ; @param (string) file-content
  ; @param (map) def
  ;
  ; @example
  ; (import-def-header "... \n; @constant (keyword)\n(def my-constant :my-value) ..." {...})
  ; =>
  ; "; @constant (keyword)"
  ;
  ; @return (strings in vector)
  [file-content {:keys [bounds]}]
  (-> file-content (string/keep-range 0 (first bounds))                      ; <- Cuts off the rest of the file content from the start position of the def.
                   (string/trim-end)                                         ; <- Removes the indent (if any) that precedes the def.
                   (regex/after-last-match #"\n[\h]{0,}\n" {:return? false}) ; <- Keeps the part after the last empty row.
                   (last-coherent-comment-row-group)))                       ; <- Extracts the last coherent comment row group.

(defn import-defn-header
  ; @ignore
  ;
  ; @description
  ; Returns the comment header of a specific defn declaration.
  ;
  ; @param (string) file-content
  ; @param (map) defn
  ;
  ; @example
  ; (import-defn-header "... (defn my-function\n; @param (map) my-param\n [my-param] ...) ..." {...})
  ; =>
  ; "; @param (map) my-param"
  ;
  ; @return (strings in vector)
  [file-content {:keys [bounds]}]
  (-> file-content (string/keep-range (first bounds) (last bounds))          ; <- Keeps the part of the file content from the start position of the defn - to its end position.
                   (regex/before-first-match #"\n[\h]{1,}\[|\n[\h]{1,}\(\[") ; <- Cuts off the part from the first (non-commented and non-quoted) argument list.
                   (last-coherent-comment-row-group)))                       ; <- Extracts the last coherent comment row group.

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn import-def-body
  ; @ignore
  ;
  ; @description
  ; Returns the source code of a specific def declaration.
  ;
  ; @param (string) file-content
  ; @param (map) def
  ;
  ; @example
  ; (import-def-body "... (def my-constant [] ...) ..." {...})
  ; =>
  ; "(def my-constant :my-value)"
  ;
  ; @return (string)
  [file-content {:keys [bounds]}]
  (string/keep-range file-content (first bounds) (last bounds)))

(defn import-defn-body
  ; @ignore
  ;
  ; @description
  ; Returns the source code of a specific defn declaration.
  ;
  ; @param (string) file-content
  ; @param (map) defn
  ;
  ; @example
  ; (import-defn-body "... (defn my-function [] ...) ..." {...})
  ; =>
  ; "(defn my-function [] ...)"
  ;
  ; @return (string)
  [file-content {:keys [bounds]}]
  (string/keep-range file-content (first bounds) (last bounds)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn import-tutorial-name
  ; @ignore
  ;
  ; @param (string) substring
  ; @param (integer) position
  ;
  ; @return (maps in vector)
  [substring position]
  (-> substring (string/keep-range position)
                (first-coherent-comment-row-group)))

(defn import-tutorial
  ; @ignore
  ;
  ; @param (string) substring
  ; @param (integer) position
  ;
  ; @return (maps in vector)
  [substring]
  (letfn [(f0 [%] (regex/re-first % #"(?<=\@tutorial[\h]{1,})[^\n]{1,}(?=\n)"))
          (f1 [%] (string/after-first-occurence % "\n"))]
         {:name    (-> substring f0)
          :content (-> substring f1 (first-coherent-comment-row-group))}))

(defn import-tutorials
  ; @ignore
  ;
  ; @param (string) file-content
  ;
  ; @return (maps in vector)
  [file-content]
  ; - The newline character could be in a positive lookbehind assertion within the regex pattern.
  ;   But unfortunatelly, the 'regex/first-dex-of' function would return incorrect positions.
  ; - The 'f1' function cuts off the part after the second match before passing the substring to
  ;   the 'first-coherent-comment-row-group' function, to prevent reading multiple tutorials
  ;   from one comment row group (if they are joined).
  (letfn [(f0 [%] (regex/first-dex-of       % #"\n[\h]{0,}\;[\h]{0,}\@tutorial"))
          (f1 [%] (regex/before-first-match % #"\n[\h]{0,}\;[\h]{0,}\@tutorial" {:return? true}))]
         (loop [substring file-content tutorials []]
               (if-let [position (f0 substring)]
                       (let [substring (string/keep-range substring (inc position))]
                            (recur substring (vector/conj-item tutorials (-> substring f1 import-tutorial))))
                       (-> tutorials)))))
