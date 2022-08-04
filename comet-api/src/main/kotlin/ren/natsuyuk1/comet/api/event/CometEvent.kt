package ren.natsuyuk1.comet.api.event

import ren.natsuyuk1.comet.api.Comet

open class CometEvent(open val comet: Comet) : AbstractEvent()
