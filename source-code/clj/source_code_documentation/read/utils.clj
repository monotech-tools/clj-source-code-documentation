
(ns source-code-documentation.read.utils
    (:require [fruits.regex.api       :as regex]
              [fruits.string.api      :as string]
              [fruits.vector.api      :as vector]
              [syntax-interpreter.api :as syntax-interpreter]
              [syntax-reader.api      :as syntax-reader]))

;; -- Snippet nomenclature ----------------------------------------------------
;; ----------------------------------------------------------------------------

; @my-marker (my-meta)(my-meta) My snippet label
; My snippet text
; =>
; {:marker :my-marker
;  :meta   ["my-meta" "my-meta"]
;  :label  "My snippet label"
;  :text   ["My snippet text"]}

;; -- Snippet example ---------------------------------------------------------
;; ----------------------------------------------------------------------------

; @param (map)(opt) options
; {:return? (boolean)(opt)
;   Default: false}
; =>
; {:marker :param
;  :meta   ["map" "opt"]}
;  :label  "options"
;  :text   ["{:return? (boolean)(opt)" "  Default: false}"]

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-section-source-code
  ; @ignore
  ;
  ; @param (map) section
  ;
  ; @usage
  ; (read-section-source-code {:name "my-function" :type :defn :content [...] :source-code "(defn my-function [])" ...})
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

(defn wrap-snippet-comments
  ; @ignore
  ;
  ; @description
  ; Wraps each commented (with double semicolon) part of content rows within the given snippet with a SPAN tag.
  ;
  ; @usage
  ; (wrap-snippet-comments {:text ["My row #1 ;; My comment"] ...})
  ; =>
  ; {:text ["My row #1 " [:span ";; My comment"]]}
  ;
  ; @return (map)
  [snippet]
  (letfn [(f0 [%] (reduce f1 [] %))
          (f1 [result row] (if-let [comment-starts-at (string/first-dex-of row ";;")]
                                   (vector/concat-items result [(string/keep-range row 0 comment-starts-at) [:span (string/keep-range row comment-starts-at)]])
                                   (vector/conj-item    result row)))]
         (-> snippet (update :text f0))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn break-snippet
  ; @ignore
  ;
  ; @description
  ; Inserts HICCUP breaks between each content row.
  ;
  ; @param (map) snippet
  ; {:text (strings in vector)(opt)
  ;  ...}
  ;
  ; @usage
  ; (break-snippet {:text ["My row #1" "My row #2"] ...}
  ; =>
  ; {:text ["My row #1" [:br] "My row #2"]
  ;  ...}
  ;
  ; @return (map)
  ; {:text (vector)
  ;  ...}
  [snippet]
  (-> snippet (update :text vector/gap-items [:br])))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn trim-snippet
  ; @ignore
  ;
  ; @description
  ; Removes the leading empty rows and trailing empty rows of the text of the given snippet.
  ;
  ; @param (map) snippet
  ; {:text (strings in vector)(opt)
  ;  ...}
  ;
  ; @usage
  ; (trim-snippet {:text ["" "" "My row #1" "" ""] ...}
  ; =>
  ; {:text ["My row #1"]
  ;  ...}
  ;
  ; @return (map)
  [snippet]
  (cond (-> snippet :text       empty?) (-> snippet)
        (-> snippet :text first empty?) (-> snippet (update :text vector/remove-first-item) trim-snippet)
        (-> snippet :text last  empty?) (-> snippet (update :text vector/remove-last-item)  trim-snippet)
        :return snippet))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-snippet
  ; @ignore
  ;
  ; @param (strings in vector) snippet
  ;
  ; @usage
  ; (read-snippet ["; @param (map)(opt) my-map"  "; {...}"])
  ; =>
  ; {:marker :param :meta ["map" "opt"] :label "my-map" :text [" {...}"] :indent 1}
  ;
  ; @return (map)
  [snippet]
  (letfn [(f0 [       %] (keyword (regex/re-first % #"(?<=\;[\h]*\@)[a-z\-\d\*]+"))) ; <- Returns the snippet marker, derived from the given comment row.
          (f1 [       %]          (regex/re-all   % #"(?<=\()[^()]+(?=\))"))         ; <- Returns the snippet meta values (if any), derived from the given comment row.
          (f2 [       %]          (regex/re-last  % #"(?<=\@.+[\h])[^\(\)]+$"))      ; <- Returns the snippet label (if any), derived from the given comment row.
          (f3 [       %] (count   (regex/re-first % #"(?<=\;)[\h]*(?=\@)")))         ; <- Returns the snippet indent length, derived from the given comment row.
          (f4 [result %] (string/keep-range % (-> result :indent inc)))              ; <- Returns the given snippet text row, with adjusted indent.
          (f5 [result %] (if (-> result empty?)                                                                        ; <- The 'result' vector is empty when the iteration reads the first row of the snippet text.
                             (-> result (merge {:marker (f0 %) :indent (f3 %)}                                         ; <- Every imported snippet starts with a snippet marker row.
                                               (let [meta  (f1 %)] (if (-> meta  vector/not-empty?) {:meta  meta}))    ; <- Meta values of snippets are optional.
                                               (let [label (f2 %)] (if (-> label string/not-empty?) {:label label})))) ; <- Labels of snippets are optional.
                             (-> result (update :text vector/conj-item (f4 result %)))))] ; <- Rows following the first row (snippet marker row) are text rows of the snippet.
         (reduce f5 {} snippet)))

(defn read-snippets
  ; @ignore
  ;
  ; @description
  ; Reads the snippets of the given section content.
  ;
  ; @param (strings in vectors in vector) section-content
  ;
  ; @usage
  ; (read-snippets [["; @param (map)(opt) my-map"  "; {...}"]
  ;                 ["; @param (vector) my-vector" "; [...]"]])
  ; =>
  ; [{:marker :param :meta ["map" "opt"] :label "my-map"    :text [[" {...}"]]}
  ;  {:marker :param :meta ["vector"]    :label "my-vector" :text [[" [...]"]]}]
  ;
  ; @return (maps in vector)
  [section-content]
  (-> section-content (vector/->items read-snippet)
                      (vector/->items trim-snippet)
                      (vector/->items break-snippet)
                      (vector/->items wrap-snippet-comments)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn split-section-content-rows
  ; @ignore
  ;
  ; @description
  ; Distributes the given section content rows into snippets.
  ;
  ; @param (strings in vector) section-content
  ;
  ; @usage
  ; (split-section-content-rows ["; @param (map) my-map" "; {...}" "; @param (vector) my-vector" "; [...]" ...])
  ; =>
  ; [["; @param (map) my-map"       "; {...}"]
  ;  ["; @param (vector) my-vector" "; [...]"] ...]
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

  ; - Snippet marker row:   "; @my-marker"
  ; - Empty comment row:    "; "
  ; - Nonempty comment row: "; abc..."
  (letfn [(f0 [%] (-> % (regex/re-match? #"^[\h]*\;[\h]*\@"))) ; <- Returns TRUE if the given comment row is a snippet marker row.
          (f1 [%] (-> % empty?))                               ; <- Returns TRUE if no snippet has been opened yet.
          (f2 [result cursor row-content] (cond (f0 row-content)    (-> result (f4 cursor row-content))   ; <- Snippet marker row
                                                :non-marker-row     (-> result (f3 cursor row-content)))) ; <- Not a snippet marker row
          (f3 [result cursor row-content] (cond (f1 result)         (-> result (f4 cursor row-content))   ; <- No snippet has been opened yet
                                                :any-snippet-opened (-> result (vector/update-last-item vector/conj-item row-content))))
          (f4 [result cursor row-content] (cond (f0 row-content)    (-> result (vector/conj-item [             row-content]))    ; <- Opening a new snippet
                                                :non-marker-row     (-> result (vector/conj-item ["; @*plain*" row-content]))))] ; <- Opening a new snippet
         ; ...
         (reduce-kv f2 [] section-content)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn remove-ignored-snippets
  ; @ignore
  ;
  ; @description
  ; Removes ignored snippets of the given section content.
  ;
  ; @param (maps in vector) section-content
  ;
  ; @return (maps in vector)
  [section-content]
  (letfn [(f0 [%] (-> % :marker (= :ignore)))]
         (vector/before-first-match section-content f0 {:return? true})))

(defn remove-empty-snippets
  ; @ignore
  ;
  ; @description
  ; Removes empty snippets of the given section content.
  ;
  ; @param (maps in vector) section-content
  ;
  ; @return (maps in vector)
  [section-content]
  (letfn [(f0 [%] (and (-> % :text empty?)
                       (-> % :marker (= :*plain*))))]
         (vector/remove-items-by section-content f0)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-section-content
  ; @ignore
  ;
  ; @param (map) section
  ;
  ; @usage
  ; (read-section-content {:name "my-function" :type :defn :content ["; Row #1" "; @param (map)(opt) options" ";  {...}" ";" "; @return (map)"] ...})
  ; =>
  ; {:label   "my-function"
  ;  :name    "my-function"
  ;  :type    :defn
  ;  :content [{:marker :param  :meta ["map" "opt"] :label "options" :text [" {...}"]}
  ;            {:marker :return :meta ["map"]}]}
  ;
  ; @return (map)
  [section]
  (-> section (update :content split-section-content-rows)
              (update :content read-snippets)
              (update :content remove-ignored-snippets)
              (update :content remove-empty-snippets)))
