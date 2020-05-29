package com.planet_ink.coffee_mud.Abilities.Prayers;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class Skill_LightPlacebo extends BardSkill
{
	@Override
	public String ID()
	{
		return "Skill_LightPlacebo";
	}

	private final static String localizedName = CMLib.lang().L("Light Placebo");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_OTHERS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_DECEPTIVE;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public long flags()
	{
		return 0;
	}

	@Override
	protected long minCastWaitTime()
	{
		return CMProps.getTickMillis()/2;
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				if(CMLib.flags().isUndead((MOB)target))
					return Ability.QUALITY_MALICIOUS;
			}
		}
		return super.castingQuality(mob,target);
	}

	protected String prayWord(final MOB mob)
	{
		if(mob.getMyDeity()!=null)
			return "pray(s) to "+mob.getMyDeity().name();
		return "pray(s)";
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;
		final boolean undead=CMLib.flags().isUndead(target);

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,(!undead?0:CMMsg.MASK_MALICIOUS)|verbalCastCode(mob,target,auto),auto?L("A faint white glow surrounds <T-NAME>."):L("^S<S-NAME> @x1, delivering a light healing touch to <T-NAMESELF>.^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final int healing=CMLib.dice().roll(2,adjustedLevel(mob,asLevel),4);
				final int oldHP=target.curState().getHitPoints();
				Ability A=null;
				for(final Enumeration<Ability> a=target.effects();a.hasMoreElements();)
				{
					final Ability A1=a.nextElement();
					if(A1 instanceof Skill_LightPlacebo)
						A=A1;
				}
				if(A==null)
					A=this.beneficialAffect(mob, target, asLevel, Integer.MAX_VALUE/3);
				if(A!=null)
				{
					if(target.curState().getHitPoints() + healing > target.maxState().getHitPoints())
						healing = mob.maxState().getHitPoints() - target.curState().getHitPoints();
					target.curState().adjHitPoints(healing, target.maxState());
					A.setMiscText("+"+healing);
					//CMLib.combat().postHealing(mob,target,this,healing,CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,null);
					if(target.curState().getHitPoints()>oldHP)
						target.tell(L("You feel a little better!"));
				}
				lastCastHelp=System.currentTimeMillis();
			}
		}
		else
			beneficialWordsFizzle(mob,target,auto?"":L("<S-NAME> @x1 for <T-NAMESELF>, but nothing happens.",prayWord(mob)));

		// return whether it worked
		return success;
	}
}
