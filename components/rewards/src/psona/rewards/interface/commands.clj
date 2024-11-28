(ns psona.rewards.interface.commands
  (:require [psona.rewards.commands :as cmds]
            [tech.merero.cqrs.interface :as cqrs]))


(cqrs/reg-command! {:handler cmds/save-interaction-command!
                    :type :rewards/save-interaction})
