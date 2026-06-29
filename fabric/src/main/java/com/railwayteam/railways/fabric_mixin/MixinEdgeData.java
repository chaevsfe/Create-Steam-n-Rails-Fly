package com.railwayteam.railways.fabric_mixin;

import com.railwayteam.railways.content.switches.TrackSwitch;
import com.railwayteam.railways.mixin_interfaces.ISwitchDisabledEdge;
import com.zurrtum.create.content.trains.graph.EdgeData;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = EdgeData.class, remap = false)
public class MixinEdgeData implements ISwitchDisabledEdge {
	private boolean enabled = true;
	private boolean automatic = false;
	private int selectPriority = -1;

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setAutomatic(boolean automatic) {
		this.automatic = automatic;
	}

	public boolean isAutomatic() {
		return automatic;
	}

	public void setAutomaticallySelected() {
		selectPriority = TrackSwitch.getSelectionPriority();
		enabled = true;
	}

	public int getAutomaticallySelectedPriority() {
		return selectPriority;
	}

	public boolean isAutomaticallySelected() {
		return selectPriority != -1;
	}

	public void ackAutomaticSelection() {
		selectPriority = -1;
	}
}
