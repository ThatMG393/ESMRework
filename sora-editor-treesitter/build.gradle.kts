/*******************************************************************************
 *    sora-editor - the awesome code editor for Android
 *    https://github.com/Rosemoe/sora-editor
 *    Copyright (C) 2020-2024  Rosemoe
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 *
 *     Please contact Rosemoe by email 2073412493@qq.com if you need
 *     additional information or have any questions
 ******************************************************************************/

plugins {
    id("com.android.library")
    id("kotlin-android")
}

group = "io.github.Rosemoe.sora-editor"
version = "0.23.4-f620608-SNAPSHOT"

android {
    namespace = "io.github.rosemoe.sora.ts"
	compileSdk = 33
	
    defaultConfig {
	    minSdk = 24
	
        consumerProguardFiles("consumer-rules.pro")
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation("com.itsaky.androidide.treesitter:android-tree-sitter:4.1.0")
	
    implementation(platform("io.github.Rosemoe.sora-editor:bom:0.23.4-f620608-SNAPSHOT"))
	implementation("io.github.Rosemoe.sora-editor:editor")
}
