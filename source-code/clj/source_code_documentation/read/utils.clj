
(ns source-code-documentation.read.utils
    (:require [fruits.regex.api       :as regex]
              [fruits.string.api      :as string]
              [fruits.vector.api      :as vector]
              [syntax-interpreter.api :as syntax-interpreter]
              [syntax-reader.api      :as syntax-reader]))

;; -- Content block nomenclature ----------------------------------------------
;; ----------------------------------------------------------------------------

; @my-block-type (my-meta)(my-meta) my-block-name
; my-block-text
; =>
; {:type :my-block-type
;  :meta ["my-meta" "my-meta"]
;  :name "my-block-name"
;  :text ["my-block-text"]}

;; -- Content block example ---------------------------------------------------
;; ----------------------------------------------------------------------------

; @param (map)(opt) options
; {:return? (boolean)(opt)
;   Default: false}
; =>
; {:type :param
;  :meta ["map" "opt"]}
;  :name "options"
;  :text ["{:return? (boolean)(opt)" "  Default: false}"]

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-section-source-code
  ; @ignore
  ;
  ; @param (map) section
  ;
  ; @usage
  ; (read-section-source-code {:name "my-function" :type :defn :content [...] :source-code "(defn my-function [])"})
  ; =>
  ; {:label       "my-function"
  ;  :name        "my-function"
  ;  :type        :defn
  ;  :content     [...]
  ;  :source-code "(defn my-function [])"}
  ;
  ; @return (map)
  [section]
  (letfn [(f0 [%] (syntax-reader/remove-tags % [(syntax-interpreter/with-options (:comment syntax-interpreter/CLJ-PATTERNS) {})
                                                (syntax-interpreter/with-options (:string  syntax-interpreter/CLJ-PATTERNS) {:keep? true})]
                                               {:keep-indents? true :remove-leftover-blank-lines? true}))]
         (-> section :type (case :def      (-> section (update :source-code f0))
                                 :defn     (-> section (update :source-code f0))
                                 :tutorial (-> section)))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn trim-section-content-block
  ; @ignore
  ;
  ; @description
  ; Removes the leading empty rows and trailing empty rows of the text of the given content block.
  ;
  ; @param (map) content-block
  ; {:text (strings in vector)(opt)
  ;  ...}
  ;
  ; @usage
  ; (trim-section-content-block {:text ["" "" "..." "" ""] ...}
  ; =>
  ; {:text ["..."]
  ;  ...}
  ;
  ; @return (map)
  [content-block]
  (cond (-> content-block :text      empty?)  (-> content-block)
        (-> content-block :text first empty?) (-> content-block (update :text vector/remove-first-item) trim-section-content-block)
        (-> content-block :text last empty?)  (-> content-block (update :text vector/remove-last-item)  trim-section-content-block)
        :return content-block))

(defn read-section-content-block
  ; @ignore
  ;
  ; @param (strings in vector) content-block
  ;
  ; @usage
  ; (read-section-content-block ["; @param (map)(opt) my-map"  "; {...}"])
  ; =>
  ; {:type :param :meta ["map" "opt"] :name "my-map" :text [" {...}"] :indent 1}
  ;
  ; @return (map)
  [content-block]
  (letfn [(f0 [       %] (keyword (regex/re-first % #"(?<=\;[\h]*\@)[a-z]+")))  ; <- Returns the block type, derived from the given comment row.
          (f1 [       %]          (regex/re-all   % #"(?<=\()[^()]+(?=\))"))    ; <- Returns the block meta values (if any), derived from the given comment row.
          (f2 [       %]          (regex/re-last  % #"(?<=\@.+[\h])[^\(\)]+$")) ; <- Returns the block name (if any), derived from the given comment row.
          (f3 [       %] (count   (regex/re-first % #"(?<=\;)[\h]*(?=\@)")))    ; <- Returns the block indent length, derived from the given comment row.
          (f4 [result %] (string/keep-range % (-> result :indent inc)))         ; <- Returns the given block text row, with adjusted indent.
          (f5 [result %] (if (-> result empty?)                                                 ; <- The 'result' vector is empty when the iteration reads the first row of block.
                             (-> result (merge {:type (f0 %) :indent (f3 %)}                    ; <- Every imported block starts with a block marker row.
                                               (let [meta (f1 %)] (if (-> meta vector/not-empty?) {:meta meta}))   ; <- Meta values of blocks are optional.
                                               (let [name (f2 %)] (if (-> name string/not-empty?) {:name name})))) ; <- Name of blocks are optional.
                             (-> result (update :text vector/conj-item (f4 result %)))))] ; <- Rows following the first row (block marker row) are text rows of the block.
         (reduce f5 {} content-block)))

(defn read-section-content-blocks
  ; @ignore
  ;
  ; @description
  ; Reads the content blocks of the given section content.
  ;
  ; @param (strings in vectors in vector) section-content
  ;
  ; @usage
  ; (read-section-content-blocks [["; @param (map)(opt) my-map"  "; {...}"]
  ;                               ["; @param (vector) my-vector" "; [...]"]])
  ; =>
  ; [{:type :param :meta ["map" "opt"] :name "my-map"    :text [" {...}"]}
  ;  {:type :param :meta ["vector"]    :name "my-vector" :text [" [...]"]}]
  ;
  ; @return (maps in vector)
  [section-content]
  (-> section-content (vector/->items read-section-content-block)
                      (vector/->items trim-section-content-block)))

(defn split-section-content-rows
  ; @ignore
  ;
  ; @description
  ; Distributes the given section content rows into content blocks.
  ;
  ; @param (strings in vector) section-content
  ;
  ; @usage
  ; (split-section-content-rows ["; @param (map) my-map" "; {...}" "; @param (vector) my-vector" "; [...]"])
  ; =>
  ; [["; @param (map) my-map"       "; {...}"]
  ;  ["; @param (vector) my-vector" "; [...]"]]
  ;
  ; @return (strings in vectors in vector)
  [section-content]

  ; Deprecated! (Previous version)
  ; - Block start marker row: "; @usage", "; @param", etc.
  ; - Block end marker row:   "; @---"
  ; - Empty comment row:      "; "
  ; - Nonempty comment row:   "; abc..."
  (letfn [(f0  [%] (-> % (regex/re-match? #"^[\h]*\;[\h]*\@")))                            ; <- Returns TRUE if the given comment row is a block marker row.
          (f1  [%] (-> % (regex/re-match? #"^[\h]*\;[\h]*\@\-\-\-")))                      ; <- Returns TRUE if the given comment row is a block end marker row.
          (f2  [%] (-> % (regex/re-match? #"^[\h]*\;[\h]*$")))                             ; <- Returns TRUE if the given comment row is an empty comment row.
          (f3  [%] (-> % (regex/re-match? #"^[\h]*\;[\h]*[^\@\h]")))                       ; <- Returns TRUE if the given comment row is an nonempty comment row.
          (f4  [%] (-> % (regex/re-first  #"^[\h]*\;[\h]*\@.*")))                          ; <- Returns the given comment row if it is a block marker row.
          (f5  [%] (-> section-content (vector/keep-range %) (vector/first-result f4) f1)) ; <- Returns TRUE if the next block marker row will be a block end marker row.
          (f6  [%] (-> % last first (= "; @separator")))                                   ; <- Returns TRUE if the last block is a separator block.
          (f7  [%] (-> % empty?))                                                          ; <- Returns TRUE if no block has been opened yet.
          (f8  [result cursor row-content] (cond (f0 row-content)  (-> result (f9  cursor row-content))   ; <- Block start marker or end marker row
                                                 (f2 row-content)  (-> result (f10 cursor row-content))   ; <- Empty comment row
                                                 (f3 row-content)  (-> result (f11 cursor row-content)))) ; <- Nonempty comment row
          (f9  [result cursor row-content] (cond (f1 row-content)  (-> result)                            ; <- Block end marker row
                                                 :start-marker     (-> result (f13 cursor row-content)))) ; <- Block start marker row
          (f10 [result cursor row-content] (cond (f5 cursor)       (-> result (f12 cursor row-content))   ; <- Empty comment row + the next block marker row will be a block end marker row.
                                                 :else             (-> result (f13 cursor row-content)))) ; <- Empty comment row + the next block marker row will NOT be a block end marker row.
          (f11 [result cursor row-content] (cond (f6 result)       (-> result (f13 cursor row-content))   ; <- Nonempty comment row + last block is a separator block
                                                 (f7 result)       (-> result (f13 cursor row-content))   ; <- Nonempty comment row + no block has been opened yet
                                                 :any-block-opened (-> result (f12 cursor row-content)))) ; <- Nonempty comment row + last block is NOT a separator block
          (f12 [result cursor row-content] (cond (f6 result)       (-> result (f13 cursor row-content))   ; <- Appends to previous block + last block is a separator block
                                                 (f7 result)       (-> result (f13 cursor row-content))   ; <- Appends to previous block + no block has been opened yet
                                                 :any-block-opened (-> result (vector/update-last-item vector/conj-item row-content))))
          (f13 [result cursor row-content] (cond (f2 row-content)  (-> result (vector/conj-item ["; @separator" row-content]))    ; <- Opens a new block + empty comment row
                                                 (f3 row-content)  (-> result (vector/conj-item ["; @plain"     row-content]))    ; <- Opens a new block + nonempty comment row
                                                 (f0 row-content)  (-> result (vector/conj-item [               row-content]))))]) ; <- Opens a new block + block marker row
         ; ...
         ;(reduce-kv f8 [] section-content))
  ; Deprecated! (Previous version)

  ; - Block marker row:     "; @usage", "; @param", etc.
  ; - Empty comment row:    "; "
  ; - Nonempty comment row: "; abc..."
  (letfn [(f0 [%] (-> % (regex/re-match? #"^[\h]*\;[\h]*\@"))) ; <- Returns TRUE if the given comment row is a block marker row.
          (f1 [%] (-> % empty?))                               ; <- Returns TRUE if no block has been opened yet.
          (f2 [result cursor row-content] (cond (f0 row-content)  (-> result (f4 cursor row-content))   ; <- Block marker row
                                                :non-marker-row   (-> result (f3 cursor row-content)))) ; <- Not a block marker row
          (f3 [result cursor row-content] (cond (f1 result)       (-> result (f4 cursor row-content))   ; <- No block has been opened yet
                                                :any-block-opened (-> result (vector/update-last-item vector/conj-item row-content))))
          (f4 [result cursor row-content] (cond (f0 row-content)  (-> result (vector/conj-item [           row-content]))    ; <- Opening a new block
                                                :non-marker-row   (-> result (vector/conj-item ["; @plain" row-content]))))] ; <- Opening a new block
         ; ...
         (reduce-kv f2 [] section-content)))

(defn clean-section-content-blocks
  ; @ignore
  ;
  ; @description
  ; Removes the ignored or empty content blocks of the given section content.
  ;
  ; @param (maps in vector) section-content
  ;
  ; @return (maps in vector)
  [section-content]
  (letfn [(f0 [%] (-> % :type (= :ignore)))
          (f1 [%] (-> % :text empty?))]
         (-> section-content (vector/before-first-match f0 {:return? true})
                             (vector/remove-items-by    f0))))

(defn read-section-content
  ; @ignore
  ;
  ; @param (map) section
  ;
  ; @usage
  ; (read-section-content {:name "my-function" :type :defn :content ["; Row #1" "; @param (map)(opt) options" ";  {...}" ";" "; @return (map)"]})
  ; =>
  ; {:label   "my-function"
  ;  :name    "my-function"
  ;  :type    :defn
  ;  :content [{:type :param  :meta ["map" "opt"] :name "options" :text [" {...}"]}
  ;            {:type :return :meta ["map"]}]}
  ;
  ; @return (map)
  [section]
  (-> section (update :content split-section-content-rows)
              (update :content read-section-content-blocks)
              (update :content clean-section-content-blocks)))
