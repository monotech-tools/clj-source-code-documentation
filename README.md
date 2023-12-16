
# source-code-documentation
...


the order of header parts doesn't matter
they will be printed in the order as they are in the header within the source code






### How to place headers of `def` declarations?

A header of a `def` declaration must precede the declaration without any empty row between them.

```
; @constant (keyword)
(def foo 42)
```

A header can be used for multiple declarations.

```
; @constant (keyword)
(def foo 42)
(def bar 69)
```

### How to place headers of `defn` declarations?

A header of a `defn` declaration must be placed between the function name and the argument list.

```
(defn foo
  ; @param (string) bar
  [bar])
```

```
(defn foo
  ; @param (string)(opt) bar
  ([])
  ([bar])
```

### How to use the `@ignore` marker?

In a `def` or a `defn` declaration's header, everything below an `@ignore` marker will be ignored
and won't be imported into the documentation files.

```
(defn foo
  ; @param (string) bar
  ;
  ; @ignore
  ; @description
  ; This description is ignored.
  ;
  ; @return (string)
  ; This return description is also ignored.
  [bar])
```

### How to use the `@redirect` marker?

Headers can be linked into each other with the `@redirect` marker.

```
; @redirect (bar)
(def foo 42)

; @description
; This header will be imported for the 'foo' symbol as well.
;
; @constant (keyword)
(def bar 69)
```

```
(defn foo
  ; @description
  ; Although the header of 'baz' function will be imported for the 'foo' function as well,
  ; the original header of the 'foo' function will be imported also.
  ;
  ; @redirect (baz)
  [bar])

(defn baz
  ; @description
  ; This description will be imported for the 'foo' function as well.
  ;
  ; @param (string) bar
  ;
  ; @return (string)
  [bar])  
```



```
; @link
ebbol lehet tobb
redirectbol egy lehet és az a source-code-ot is átirányitja
```


```
; @preview (my-screenshot.png)
; (my-function :my-param)
; =>
; "My return value"
```





```
; @redirect
; Imports the header of the 'foo' declaration from the 'bar' namespace.
(def foo bar/foo)
```

```
; @redirect (*)
; Imports the header of the 'baz' declaration from the 'bar' namespace.
(def foo bar/baz)
```

```
; @redirect (*/*)
(def foo bar/foo)
```

```
; @redirect (bar/foo)
; Imports the header of the 'foo' declaration from the 'bar' namespace.
(def foo bar/foo)
```

```
; @redirect (boo/*)
; Imports the header of the 'baz' declaration from the 'boo' namespace.
(def foo bar/baz)
```

```
; @redirect (*/boo)
; Imports the header of the 'boo' declaration from the 'bar' namespace.
(def foo bar/baz)
```

```
; @redirect (baz.*/*)
(def foo bar/foo)
```

```
; @redirect (baz.bar/*)
(def foo bar/foo)
```



; @redirect (namespace/*)  ; <- uses headers of namespace/a and namespace/b
(def a "a")
(def b "b")

(defn c
  ; @redirect (namespace/*)  ; <- uses headers of namespace/c
  [])

; @redirect (namespace.*/*) ; <- uses headers of namespace.x/a
(def a x/a)

; @redirect (*/*) ; <- uses headers of namespace.x/a
(def a namespace.x/a)

; @redirect (namespace/*) ; <- the redirected headers are prepended/appended/inserted to the normal headers
; @constant (str)         ; <- uses the normal headers as well
(def a "a")

; @redirect (namespace/z)  ; <- uses headers of namespace/z
(def a "a")




```
; @tutorial
; ...
```


```
; My text [My link](#my-anchor)
; My text [My link](https://...)
```

```
@note
```
