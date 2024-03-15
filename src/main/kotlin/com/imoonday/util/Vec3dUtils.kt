package com.imoonday.util

import net.minecraft.util.math.Vec3d

operator fun Vec3d.plus(vec: Vec3d): Vec3d = Vec3d(x + vec.x, y + vec.y, z + vec.z)

operator fun Vec3d.minus(vec: Vec3d): Vec3d = Vec3d(x - vec.x, y - vec.y, z - vec.z)

operator fun Vec3d.times(vec: Vec3d): Vec3d = Vec3d(x * vec.x, y * vec.y, z * vec.z)

operator fun Vec3d.div(vec: Vec3d): Vec3d = Vec3d(x / vec.x, y / vec.y, z / vec.z)