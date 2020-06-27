package com.projectkorra.projectkorra.firebending;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.plant.PlantRegrowth;

public class BlazeArc extends FireAbility {

	private static final long DISSIPATE_REMOVE_TIME = 400;

	private long time;
	private long interval;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute(Attribute.SPEED)
	private double speed;
	private Location origin;
	private Location location;
	private Vector direction;

	public BlazeArc(final Player player, final Location location, final Vector direction, final double range) {
		super(player);
		this.range = this.getDayFactor(range);
		this.speed = getConfig().getLong("Abilities.Fire.Blaze.Speed");
		this.interval = (long) (1000. / this.speed);
		this.origin = location.clone();
		this.location = this.origin.clone();

		this.direction = direction.clone().setY(0).normalize();
		this.location = this.location.clone().add(this.direction);

		this.time = System.currentTimeMillis();
		this.start();
	}

	private void ignite(final Block block) {
		if (!GeneralMethods.isSolid(block.getRelative(BlockFace.DOWN))) {
			return;
		}
		
		if (!isFire(block) && !isAir(block.getType())) {
			if (canFireGrief()) {
				if (isPlant(block) || isSnow(block)) {
					new PlantRegrowth(this.player, block);
				}
			}
		}

		new TempBlock(block, getFireColor()).setRevertTime(DISSIPATE_REMOVE_TIME);
	}

	@Override
	public void progress() {
		if (!this.bPlayer.canBendIgnoreBindsCooldowns(this)) {
			this.remove();
			return;
		} else if (System.currentTimeMillis() - this.time >= this.interval) {
			this.location.add(this.direction);
			this.time = System.currentTimeMillis();

			final Block block = this.location.getBlock();
			if (isFire(block)) {
				return;
			}

			if (this.location.distanceSquared(this.origin) > this.range * this.range) {
				this.remove();
				return;
			} else if (GeneralMethods.isRegionProtectedFromBuild(this, this.location)) {
				return;
			}

			final Block ignitable = getIgnitable(block, 0, 2);
			if (ignitable != null) {
				this.ignite(ignitable);
			}
		}
	}

	public static void removeAroundPoint(final Location location, final double radius) {
		for (final BlazeArc stream : getAbilities(BlazeArc.class)) {
			if (stream.location.getWorld().equals(location.getWorld())) {
				if (stream.location.distanceSquared(location) <= radius * radius) {
					stream.remove();
				}
			}
		}
	}

	@Override
	public String getName() {
		return "Blaze";
	}

	@Override
	public Location getLocation() {
		if (this.location != null) {
			return this.location;
		}
		return this.origin;
	}

	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public long getTime() {
		return this.time;
	}

	public void setTime(final long time) {
		this.time = time;
	}

	public long getInterval() {
		return this.interval;
	}

	public void setInterval(final long interval) {
		this.interval = interval;
	}

	public double getRange() {
		return this.range;
	}

	public void setRange(final double range) {
		this.range = range;
	}

	public double getSpeed() {
		return this.speed;
	}

	public void setSpeed(final double speed) {
		this.speed = speed;
	}

	public Location getOrigin() {
		return this.origin;
	}

	public void setOrigin(final Location origin) {
		this.origin = origin;
	}

	public Vector getDirection() {
		return this.direction;
	}

	public void setDirection(final Vector direction) {
		this.direction = direction;
	}

	public static long getDissipateRemoveTime() {
		return DISSIPATE_REMOVE_TIME;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

}
