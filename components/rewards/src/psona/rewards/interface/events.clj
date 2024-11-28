(ns psona.rewards.interface.events
  (:require [tech.merero.cqrs.interface :as cqrs]
            [psona.rewards.commands :as cmds]))


(cqrs/reg-event-handler
 :rewards/free-beverage-reward-acquired
 :rewards/create-last-reward-projection
 cmds/create-last-reward-projection-action!)


(cqrs/reg-event-handler
 :rewards/free-beverage-reward-acquired
 :rewards/save-new-reward
 cmds/create-new-reward-action!)


(comment
  )
