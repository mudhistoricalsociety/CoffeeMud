package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.system.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Meditation extends StdAbility
{
	public String ID() { return "Skill_Meditation"; }
	public String name(){ return "Meditation";}
	public String displayText(){ return "(Meditating)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"MEDITATE"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public Environmental newInstance(){	return new Skill_Meditation();}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();
		if(canBeUninvoked())
		{
			if(!mob.amDead())
			{
				if(mob.location()!=null)
					mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> end(s) <S-HIS-HER> meditation.");
				else
					mob.tell("Your meditation ends.");
			}
		}
	}

	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every message listed in the CMMsg interface
	 * from the given Environmental source */
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		if((msg.amISource(mob))
		&&(!Util.bset(msg.sourceCode(),CMMsg.MASK_CHANNEL))
		&&((Util.bset(msg.sourceCode(),CMMsg.MASK_MOVE))||(Util.bset(msg.sourceCode(),CMMsg.MASK_HANDS))||(Util.bset(msg.sourceCode(),CMMsg.MASK_MOUTH))))
			unInvoke();
		if(Util.bset(msg.othersCode(),CMMsg.MASK_SOUND)
		   &&(Sense.canBeHeardBy(msg.source(),mob)))
		{
			if(!msg.amISource(mob))
				msg.addTrailerMsg(new FullMsg(mob,null,null,CMMsg.TYP_GENERAL|CMMsg.MASK_HANDS,CMMsg.NO_EFFECT,CMMsg.NO_EFFECT,"Your meditation is interrupted by the noise."));
			else
				msg.addTrailerMsg(new FullMsg(mob,null,null,CMMsg.TYP_GENERAL|CMMsg.MASK_HANDS,CMMsg.NO_EFFECT,CMMsg.NO_EFFECT,"Your meditation is interrupted."));
		}
		return;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(ticking,tickID);

		MOB mob=(MOB)affected;

		if(tickID!=MudHost.TICK_MOB) return true;
		if(!profficiencyCheck(0,false)) return true;

		if((mob.curState().getHunger()<=0)
		||(mob.curState().getThirst()<=0))
		{
			mob.tell("Your stomach growls!");
			unInvoke();
			return false;
		}

		if((!mob.isInCombat())
		&&(Sense.isSitting(mob)))
		{
			double man=new Integer((mob.charStats().getStat(CharStats.INTELLIGENCE)+mob.charStats().getStat(CharStats.WISDOM))).doubleValue();
			mob.curState().adjMana((int)Math.round((man*.1)+(mob.envStats().level()/2)),mob.maxState());
		}
		else
		{
			unInvoke();
			return false;
		}
		return super.tick(ticking,tickID);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if(mob.isInCombat())
		{
			mob.tell("You can't meditate while in combat!");
			return false;
		}
		if(!Sense.isSitting(mob))
		{
			mob.tell("You must be in a sitting, restful position to meditate.");
			return false;
		}
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell("You are already meditating!");
			return false;
		}
		// now see if it worked
		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_OK_VISUAL|(auto?CMMsg.MASK_GENERAL:0),auto?"":"<S-NAME> begin(s) to meditate...");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,Integer.MAX_VALUE-1000);
				helpProfficiency(mob);
			}
		}
		else
			return beneficialVisualFizzle(mob,target,"<S-NAME> attempt(s) to meditate, but lose(s) concentration.");

		// return whether it worked
		return success;
	}
}