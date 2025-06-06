/*
 * Copyright (c) 2025, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2025, Red Hat Inc.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */


/*
 * @test
 * @summary Test PrintVMInfoAtExit
 * @library /test/lib
 * @modules java.base/jdk.internal.misc
 * @requires vm.flagless
 * @requires vm.bits == "64"
 * @run driver PrintVMInfoAtExitTest
 */

import jdk.test.lib.process.OutputAnalyzer;
import jdk.test.lib.process.ProcessTools;

public class PrintVMInfoAtExitTest {

  public static void main(String[] args) throws Exception {
    ProcessBuilder pb = ProcessTools.createLimitedTestJavaProcessBuilder(
            "-Xmx64M", "-Xms64M",
            "-XX:-CreateCoredumpOnCrash",
            "-XX:+UnlockDiagnosticVMOptions",
            "-XX:+PrintVMInfoAtExit",
            "-XX:NativeMemoryTracking=summary",
            "-XX:CompressedClassSpaceSize=256m",
            "-version");

    OutputAnalyzer output_detail = new OutputAnalyzer(pb.start());
    output_detail.shouldContain("# JRE version:");
    output_detail.shouldContain("--  S U M M A R Y --");
    output_detail.shouldContain("Command Line: -Xmx64M -Xms64M -XX:-CreateCoredumpOnCrash -XX:+UnlockDiagnosticVMOptions -XX:+PrintVMInfoAtExit -XX:NativeMemoryTracking=summary -XX:CompressedClassSpaceSize=256m");
    output_detail.shouldContain("Native Memory Tracking:");
    output_detail.shouldContain("Java Heap (reserved=65536KB, committed=65536KB)");
  }
}


