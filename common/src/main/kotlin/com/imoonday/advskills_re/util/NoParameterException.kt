package com.imoonday.advskills_re.util

import com.imoonday.advskills_re.skill.*

class NoParameterException(skill: Skill, parameter: String) :
    IllegalStateException("Skill ${skill.id} has no parameter $parameter")