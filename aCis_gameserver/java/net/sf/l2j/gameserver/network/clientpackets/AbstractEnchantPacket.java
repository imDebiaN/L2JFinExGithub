package net.sf.l2j.gameserver.network.clientpackets;

import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.finex.enums.EGradeType;
import net.sf.l2j.gameserver.model.item.type.WeaponType;

public abstract class AbstractEnchantPacket extends L2GameClientPacket {

	public static final Map<Integer, EnchantScroll> SCROLLS = new HashMap<>();

	public static final class EnchantScroll {

		protected final boolean _isWeapon;
		protected final EGradeType _grade;
		private final boolean _isBlessed;
		private final boolean _isCrystal;

		public EnchantScroll(boolean wep, boolean bless, boolean crystal, EGradeType type) {
			_isWeapon = wep;
			_grade = type;
			_isBlessed = bless;
			_isCrystal = crystal;
		}

		/**
		 * @param enchantItem : The item to enchant.
		 * @return true if support item can be used for this item
		 */
		public final boolean isValid(ItemInstance enchantItem) {
			if (enchantItem == null) {
				return false;
			}

			// checking scroll type and configured maximum enchant level
			switch (enchantItem.getItem().getType2()) {
				case Item.TYPE2_WEAPON:
					if (!_isWeapon || (Config.ENCHANT_MAX_WEAPON > 0 && enchantItem.getEnchantLevel() >= Config.ENCHANT_MAX_WEAPON)) {
						return false;
					}
					break;

				case Item.TYPE2_SHIELD_ARMOR:
				case Item.TYPE2_ACCESSORY:
					if (_isWeapon || (Config.ENCHANT_MAX_ARMOR > 0 && enchantItem.getEnchantLevel() >= Config.ENCHANT_MAX_ARMOR)) {
						return false;
					}
					break;

				default:
					return false;
			}

			// check for crystal type
			if (_grade != enchantItem.getItem().getCrystalType()) {
				return false;
			}

			return true;
		}

		/**
		 * @return true if item is a blessed scroll.
		 */
		public final boolean isBlessed() {
			return _isBlessed;
		}

		/**
		 * @return true if item is a crystal scroll.
		 */
		public final boolean isCrystal() {
			return _isCrystal;
		}

		/**
		 * Regarding enchant system :<br>
		 * <br>
		 * <u>Weapons</u>
		 * <ul>
		 * <li>magic weapons has chance of 40% until +15 and 20% from +15 and
		 * higher. There is no upper limit, there is no dependance on current
		 * enchant level.</li>
		 * <li>non magic weapons has chance of 70% until +15 and 35% from +15
		 * and higher. There is no upper limit, there is no dependance on
		 * current enchant level.</li>
		 * </ul>
		 * <u>Armors</u>
		 * <ul>
		 * <li>non fullbody armors (jewelry, upper armor, lower armor, boots,
		 * gloves, helmets and shirts) has chance of 2/3 for +4, 1/3 for +5, 1/4
		 * for +6, ...., 1/18 +20. If you've made a +20 armor, chance to make it
		 * +21 will be equal to zero (0%).</li>
		 * <li>full body armors has a chance of 1/1 for +4, 2/3 for +5, 1/3 for
		 * +6, ..., 1/17 for +20. If you've made a +20 armor, chance to make it
		 * +21 will be equal to zero (0%).</li>
		 * </ul>
		 *
		 * @param enchantItem : The item to enchant.
		 * @return the enchant chance under double format (0.7 / 0.35 /
		 * 0.44324...).
		 */
		public final double getChance(ItemInstance enchantItem) {
			if (!isValid(enchantItem)) {
				return -1;
			}

			boolean fullBody = enchantItem.getItem().getBodyPart() == Item.SLOT_FULL_ARMOR;
			if (enchantItem.getEnchantLevel() < Config.ENCHANT_SAFE_MAX || (fullBody && enchantItem.getEnchantLevel() < Config.ENCHANT_SAFE_MAX_FULL)) {
				return 1;
			}

			double chance = 0;

			// Armor formula : 0.66^(current-2), chance is lower and lower for each enchant.
			if (enchantItem.isArmor()) {
				chance = Math.pow(Config.ENCHANT_CHANCE_ARMOR, (enchantItem.getEnchantLevel() - 2));
			} // Weapon formula is 70% for fighter weapon, 40% for mage weapon. Special rates after +14.
			else if (enchantItem.isWeapon()) {
				if (((Weapon) enchantItem.getItem()).isMagical()) {
					chance = (enchantItem.getEnchantLevel() > 14) ? Config.ENCHANT_CHANCE_WEAPON_MAGIC_15PLUS : Config.ENCHANT_CHANCE_WEAPON_MAGIC;
				} else {
					chance = (enchantItem.getEnchantLevel() > 14) ? Config.ENCHANT_CHANCE_WEAPON_NONMAGIC_15PLUS : Config.ENCHANT_CHANCE_WEAPON_NONMAGIC;
				}
			}

			return chance;
		}
	}

	/**
	 * Format : itemId, (isWeapon, isBlessed, isCrystal, grade)<br>
	 * Allowed items IDs must be sorted by ascending order.
	 */
	static {
		// Scrolls: Enchant Weapon
		SCROLLS.put(729, new EnchantScroll(true, false, false, EGradeType.A));
		SCROLLS.put(947, new EnchantScroll(true, false, false, EGradeType.B));
		SCROLLS.put(951, new EnchantScroll(true, false, false, EGradeType.C));
		SCROLLS.put(955, new EnchantScroll(true, false, false, EGradeType.D));
		SCROLLS.put(959, new EnchantScroll(true, false, false, EGradeType.S));

		// Scrolls: Enchant Armor
		SCROLLS.put(730, new EnchantScroll(false, false, false, EGradeType.A));
		SCROLLS.put(948, new EnchantScroll(false, false, false, EGradeType.B));
		SCROLLS.put(952, new EnchantScroll(false, false, false, EGradeType.C));
		SCROLLS.put(956, new EnchantScroll(false, false, false, EGradeType.D));
		SCROLLS.put(960, new EnchantScroll(false, false, false, EGradeType.S));

		// Blessed Scrolls: Enchant Weapon
		SCROLLS.put(6569, new EnchantScroll(true, true, false, EGradeType.A));
		SCROLLS.put(6571, new EnchantScroll(true, true, false, EGradeType.B));
		SCROLLS.put(6573, new EnchantScroll(true, true, false, EGradeType.C));
		SCROLLS.put(6575, new EnchantScroll(true, true, false, EGradeType.D));
		SCROLLS.put(6577, new EnchantScroll(true, true, false, EGradeType.S));

		// Blessed Scrolls: Enchant Armor
		SCROLLS.put(6570, new EnchantScroll(false, true, false, EGradeType.A));
		SCROLLS.put(6572, new EnchantScroll(false, true, false, EGradeType.B));
		SCROLLS.put(6574, new EnchantScroll(false, true, false, EGradeType.C));
		SCROLLS.put(6576, new EnchantScroll(false, true, false, EGradeType.D));
		SCROLLS.put(6578, new EnchantScroll(false, true, false, EGradeType.S));

		// Crystal Scrolls: Enchant Weapon
		SCROLLS.put(731, new EnchantScroll(true, false, true, EGradeType.A));
		SCROLLS.put(949, new EnchantScroll(true, false, true, EGradeType.B));
		SCROLLS.put(953, new EnchantScroll(true, false, true, EGradeType.C));
		SCROLLS.put(957, new EnchantScroll(true, false, true, EGradeType.D));
		SCROLLS.put(961, new EnchantScroll(true, false, true, EGradeType.S));

		// Crystal Scrolls: Enchant Armor
		SCROLLS.put(732, new EnchantScroll(false, false, true, EGradeType.A));
		SCROLLS.put(950, new EnchantScroll(false, false, true, EGradeType.B));
		SCROLLS.put(954, new EnchantScroll(false, false, true, EGradeType.C));
		SCROLLS.put(958, new EnchantScroll(false, false, true, EGradeType.D));
		SCROLLS.put(962, new EnchantScroll(false, false, true, EGradeType.S));
	}

	/**
	 * @param scroll The instance of item to make checks on.
	 * @return enchant template for scroll.
	 */
	protected static final EnchantScroll getEnchantScroll(ItemInstance scroll) {
		return SCROLLS.get(scroll.getItemId());
	}

	/**
	 * @param item The instance of item to make checks on.
	 * @return true if item can be enchanted.
	 */
	protected static final boolean isEnchantable(ItemInstance item) {
		if (item.isHeroItem() || item.isShadowItem() || item.isEtcItem() || item.getItem().getItemType() == WeaponType.FISHINGROD) {
			return false;
		}

		// only equipped items or in inventory can be enchanted
		if (item.getLocation() != ItemInstance.ItemLocation.INVENTORY && item.getLocation() != ItemInstance.ItemLocation.PAPERDOLL) {
			return false;
		}

		return true;
	}
}
