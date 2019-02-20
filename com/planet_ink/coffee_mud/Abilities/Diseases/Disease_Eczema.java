package com.planet_ink.coffee_mud.Abilities.Diseases;
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
   Copyright 2019-2019 Bo Zimmerman

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
public class Disease_Eczema extends Disease
{
	@Override
	public String ID()
	{
		return "Disease_Eczema";
	}

	private final static String localizedName = CMLib.lang().L("Eczema");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Eczema)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
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
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	@Override
	public int difficultyLevel()
	{
		return 2;
	}

	@Override
	protected int DISEASE_TICKS()
	{
		return getTicksPerDay();
	}

	@Override
	protected int DISEASE_DELAY()
	{
		return CMLib.dice().roll(1, 5, 5);
	}

	@Override
	protected String DISEASE_DONE()
	{
		return L("Your dry-skin problem clears up.");
	}

	@Override
	protected String DISEASE_START()
	{
		return L("^G<S-YOUPOSS> skin dr(ys) up!^?");
	}

	@Override
	protected String DISEASE_AFFECT()
	{
		return L("<S-NAME> scratch(es) <S-HIM-HER> skin.");
	}

	@Override
	public int spreadBitmap()
	{
		return 0;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(affected==null)
			return false;
		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;
		if((!mob.amDead())&&((--diseaseTick)<=0))
		{
			diseaseTick=DISEASE_DELAY();
			mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,DISEASE_AFFECT());
			catchIt(mob);
			return true;
		}
		return true;
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		if(affected==null)
			return;
		affectableStats.setStat(CharStats.STAT_DEXTERITY,affectableStats.getStat(CharStats.STAT_DEXTERITY)-3);
		if(affectableStats.getStat(CharStats.STAT_DEXTERITY)<=0)
			affectableStats.setStat(CharStats.STAT_DEXTERITY,1);
		affectableStats.setStat(CharStats.STAT_CHARISMA,affectableStats.getStat(CharStats.STAT_CHARISMA)-3);
		if(affectableStats.getStat(CharStats.STAT_CHARISMA)<=0)
			affectableStats.setStat(CharStats.STAT_CHARISMA,1);
	}
}