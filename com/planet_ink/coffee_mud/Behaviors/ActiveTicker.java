package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class ActiveTicker extends StdBehavior
{
	public String ID(){return "ActiveTicker";}
	protected int canImproveCode(){return Behavior.CAN_ITEMS|Behavior.CAN_MOBS|Behavior.CAN_ROOMS|Behavior.CAN_EXITS|Behavior.CAN_AREAS;}

	protected int minTicks=10;
	protected int maxTicks=30;
	protected int chance=100;
	protected int tickDown=(int)Math.round(Math.random()*(maxTicks-minTicks))+minTicks;

	protected void tickReset()
	{
		tickDown=(int)Math.round(Math.random()*(maxTicks-minTicks))+minTicks;
	}

	public Behavior newInstance()
	{
		return new ActiveTicker();
	}

	public void setParms(String newParms)
	{
		parms=newParms;
		minTicks=Util.getParmInt(parms,"min",minTicks);
		maxTicks=Util.getParmInt(parms,"max",maxTicks);
		chance=Util.getParmInt(parms,"chance",chance);
		tickReset();
	}

	public String getParmsNoTicks()
	{
		String parms=getParms();
		char c=';';
		int x=parms.indexOf(c);
		if(x<0){ c='/'; x=parms.indexOf(c);}
		if(x>0)
		{
			if((x+1)>parms.length())
				return "";
			parms=parms.substring(x+1);
		}
		else
		{
			return "";
		}
		return parms;
	}

	protected boolean canAct(Tickable ticking, int tickID)
	{
		if((tickID==MudHost.TICK_MOB)
		||(tickID==MudHost.TICK_ITEM_BEHAVIOR)
		||(tickID==MudHost.TICK_ROOM_BEHAVIOR)
		||((tickID==MudHost.TICK_AREA)&&(ticking instanceof Area)))
		{
			int a=Dice.rollPercentage();
			if((--tickDown)<1)
			{
				tickReset();
				if((ticking instanceof MOB)&&(!canActAtAll(ticking)))
					return false;
				if(a>chance)
					return false;
				return true;
			}
		}
		return false;
	}
}
