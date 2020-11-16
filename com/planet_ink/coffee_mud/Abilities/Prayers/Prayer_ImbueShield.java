package com.planet_ink.coffee_mud.Abilities.Prayers;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2020-2020 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Prayer_ImbueShield extends Prayer
{

	@Override
	public String ID()
	{
		return "Prayer_ImbueShield";
	}

	private final static String	localizedName	= CMLib.lang().L("Imbue Shield");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER | Ability.DOMAIN_BLESSING;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_NOORDERING;
	}

	@Override
	protected int overrideMana()
	{
		return Ability.COST_ALL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(commands.size()<2)
		{
			mob.tell(L("Imbue which prayer onto what?"));
			return false;
		}
		final Physical target=mob.location().fetchFromMOBRoomFavorsItems(mob,null,commands.get(commands.size()-1),Wearable.FILTER_UNWORNONLY);
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell(L("You don't see '@x1' here.",(commands.get(commands.size()-1))));
			return false;
		}
		if(!(target instanceof Shield))
		{
			mob.tell(mob,target,null,L("You can't imbue <T-NAME>."));
			return false;
		}

		commands.remove(commands.size()-1);
		final Item targetI=(Item)target;

		final String prayerName=CMParms.combine(commands,0).trim();
		Ability imbuePrayerA=null;
		final List<Ability> ables=new ArrayList<Ability>();
		for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)
			&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
			&&((!A.isSavable())||(CMLib.ableMapper().qualifiesByLevel(mob,A)))
			&&(!A.ID().equals(this.ID())))
				ables.add(A);
		}
		imbuePrayerA = (Ability)CMLib.english().fetchEnvironmental(ables,prayerName,true);
		if(imbuePrayerA==null)
			imbuePrayerA = (Ability)CMLib.english().fetchEnvironmental(ables,prayerName,false);
		if(imbuePrayerA==null)
		{
			mob.tell(L("You don't know how to imbue anything with '@x1'.",prayerName));
			return false;
		}

		if((imbuePrayerA.ID().equals("Spell_Stoneskin"))
		||(imbuePrayerA.ID().equals("Spell_MirrorImage"))
		||(CMath.bset(imbuePrayerA.flags(), FLAG_SUMMONING))
		||(imbuePrayerA.canAffect(CAN_ROOMS)))
		{
			mob.tell(L("That prayer cannot be used to imbue anything."));
			return false;
		}

		if((CMLib.ableMapper().lowestQualifyingLevel(imbuePrayerA.ID())>24)
		||(((StdAbility)imbuePrayerA).usageCost(null,true)[0]>45)
		||(CMath.bset(imbuePrayerA.flags(), Ability.FLAG_CLANMAGIC)))
		{
			mob.tell(L("That prayer is too powerful to imbue into anything."));
			return false;
		}

		if((targetI.numEffects()>0)||(!targetI.isGeneric()))
		{
			mob.tell(L("You can't imbue '@x1'.",targetI.name()));
			return false;
		}

		int experienceToLose=1000;
		experienceToLose+=(100*CMLib.ableMapper().lowestQualifyingLevel(imbuePrayerA.ID()));
		if((mob.getExperience()-experienceToLose)<0)
		{
			mob.tell(L("You don't have enough experience to use this magic."));
			return false;
		}
		// lose all the mana!
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			experienceToLose=getXPCOSTAdjustment(mob,experienceToLose);
			experienceToLose=-CMLib.leveler().postExperience(mob,null,null,-experienceToLose,false);
			mob.tell(L("You lose @x1 experience points for the effort.",""+experienceToLose));
			setMiscText(imbuePrayerA.ID());
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),L("<S-NAME> @x1 while holding <T-NAMESELF> tightly, and looking very frustrated.",super.prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,null,CMMsg.MSG_OK_VISUAL,L("<T-NAME> glow(s) brightly!"));
				targetI.basePhyStats().setDisposition(target.basePhyStats().disposition()|PhyStats.IS_BONUS);
				targetI.basePhyStats().setLevel(targetI.basePhyStats().level()+(CMLib.ableMapper().lowestQualifyingLevel(imbuePrayerA.ID())/2));
				//Vector<String> V=CMParms.parseCommas(CMLib.utensils().wornList(wand.rawProperLocationBitmap()),true);
				/*
				if(wand instanceof Armor)
				{
					final Ability A=CMClass.getAbility("Prop_WearSpellCast");
					A.setMiscText("LAYERED;"+imbuePrayerA.ID()+";");
					wand.addNonUninvokableEffect(A);
				}
				else
				if(wand instanceof Weapon)
				{
					final Ability A=CMClass.getAbility("Prop_FightSpellCast");
					A.setMiscText("25%;MAXTICKS=12;"+imbuePrayerA.ID()+";");
					wand.addNonUninvokableEffect(A);
				}
				else
				if((wand instanceof Food)
				||(wand instanceof Drink))
				{
					final Ability A=CMClass.getAbility("Prop_UseSpellCast2");
					A.setMiscText(imbuePrayerA.ID()+";");
					wand.addNonUninvokableEffect(A);
				}
				else
				*/
				if(targetI.fitsOn(Wearable.WORN_HELD)||targetI.fitsOn(Wearable.WORN_WIELD))
				{
					final Ability A=CMClass.getAbility("Prop_WearSpellCast");
					A.setMiscText("LAYERED;"+imbuePrayerA.ID()+";");
					targetI.addNonUninvokableEffect(A);
				}
				/*
				else
				{
					final Ability A=CMClass.getAbility("Prop_WearSpellCast");
					A.setMiscText("LAYERED;"+imbuePrayerA.ID()+";");
					wand.addNonUninvokableEffect(A);
				}
				*/
				targetI.recoverPhyStats();
			}

		}
		else
		{
			experienceToLose=getXPCOSTAdjustment(mob,experienceToLose);
			experienceToLose=-CMLib.leveler().postExperience(mob,null,null,-experienceToLose,false);
			mob.tell(L("You lose @x1 experience points for the effort.",""+experienceToLose));
			beneficialWordsFizzle(mob,target,L("<S-NAME> @x1 while holding <T-NAMESELF> tightly, and looking very frustrated.",super.prayWord(mob)));
		}
		// return whether it worked
		return success;
	}
}
