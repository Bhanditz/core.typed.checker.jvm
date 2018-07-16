(ns clojure.core.typed.check.const
  (:require [clojure.core.typed.object-rep :as obj]
            [clojure.core.typed.lex-env :as lex]
            [clojure.core.typed.utils :as u]
            [clojure.core.typed.filter-rep :as fl]
            [clojure.core.typed.filter-ops :as fo]
            [clojure.core.typed.check-below :as below]
            [clojure.core.typed.type-rep :as r]))

(defn flow-for-value []
  (let [props (:props (lex/lexical-env))
        flow (r/-flow (apply fo/-and fl/-top props))]
    flow))

(defn filter-for-value [val]
  (if val
    (fo/-FS fl/-top fl/-bot)
    (fo/-FS fl/-bot fl/-top)))

(defn check-const
  "Given a :const node and an expected type returns a new :const
  node annotated with its type.
  
  quoted? should be true if this :const node is nested inside a
  :quote node, otherwise should be false"
  [constant-type quoted? {:keys [val] :as expr} expected]
  {:pre [(#{:const} (:op expr))
         ((some-fn nil? r/TCResult?) expected)]
   :post [(-> % u/expr-type r/TCResult?)]}
  (let [inferred-ret (r/ret (constant-type val quoted?)
                            (filter-for-value val)
                            obj/-empty
                            (flow-for-value))]
    (assoc expr
           u/expr-type (below/maybe-check-below inferred-ret expected))))
