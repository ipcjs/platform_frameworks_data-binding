/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.databinding.tool;

import com.android.annotations.Nullable;
import com.google.common.collect.Sets;

import android.databinding.tool.util.Preconditions;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class is used to pass information from the build system into the data binding compiler.
 * It can serialize itself to a given list of annotation processor options and read itself
 * from there.
 */
@SuppressWarnings("unused")
public class DataBindingCompilerArgs {
    private static final String PREFIX = "android.databinding.";
    // the folder used by data binding to read / write data about the build process
    private static final String PARAM_BUILD_FOLDER = PREFIX + "bindingBuildFolder";

    // the folder where generational class files are exported, only set in library builds
    private static final String PARAM_AAR_OUT_FOLDER = PREFIX + "generationalFileOutDir";

    private static final String PARAM_SDK_DIR = PREFIX + "sdkDir";

    private static final String PARAM_IS_LIBRARY = PREFIX + "isLibrary";

    private static final String PARAM_XML_OUT_DIR = PREFIX + "xmlOutDir";

    private static final String PARAM_EXPORT_CLASS_LIST_TO = PREFIX + "exportClassListTo";

    private static final String PARAM_MODULE_PKG = PREFIX + "modulePackage";

    private static final String PARAM_MIN_API = PREFIX + "minApi";

    private static final String PARAM_ENABLE_DEBUG_LOGS = PREFIX + "enableDebugLogs";

    private static final String PARAM_PRINT_ENCODED_ERROR_LOGS = PREFIX + "printEncodedErrors";

    public static final Set<String> ALL_PARAMS = Sets.newHashSet( PARAM_BUILD_FOLDER,
            PARAM_AAR_OUT_FOLDER, PARAM_SDK_DIR, PARAM_IS_LIBRARY, PARAM_XML_OUT_DIR,
            PARAM_EXPORT_CLASS_LIST_TO, PARAM_MODULE_PKG, PARAM_MIN_API,
            PARAM_ENABLE_DEBUG_LOGS, PARAM_PRINT_ENCODED_ERROR_LOGS);

    private String mBuildFolder;
    private String mAarOutFolder;
    private String mSdkDir;
    private String mXmlOutDir;
    private String mExportClassListTo;
    private String mModulePackage;
    private int mMinApi;
    private boolean mIsLibrary;
    private boolean mEnableDebugLogs;
    private boolean mPrintEncodedErrorLogs;

    private DataBindingCompilerArgs() {}

    public static DataBindingCompilerArgs readFromOptions(Map<String, String> options) {
        DataBindingCompilerArgs args = new DataBindingCompilerArgs();
        args.mBuildFolder = options.get(PARAM_BUILD_FOLDER);
        args.mAarOutFolder = options.get(PARAM_AAR_OUT_FOLDER);
        args.mSdkDir = options.get(PARAM_SDK_DIR);
        args.mXmlOutDir = options.get(PARAM_XML_OUT_DIR);
        args.mExportClassListTo = options.get(PARAM_EXPORT_CLASS_LIST_TO);
        args.mModulePackage = options.get(PARAM_MODULE_PKG);
        args.mMinApi = Integer.parseInt(options.get(PARAM_MIN_API));
        args.mIsLibrary = deserialize(options.get(PARAM_IS_LIBRARY));
        args.mEnableDebugLogs = deserialize(options.get(PARAM_ENABLE_DEBUG_LOGS));
        args.mPrintEncodedErrorLogs = deserialize(options.get(PARAM_PRINT_ENCODED_ERROR_LOGS));
        return args;
    }

    @Nullable
    public String getBuildFolder() {
        return mBuildFolder;
    }

    @Nullable
    public String getAarOutFolder() {
        return mAarOutFolder;
    }

    public String getSdkDir() {
        return mSdkDir;
    }

    public String getXmlOutDir() {
        return mXmlOutDir;
    }

    public String getExportClassListTo() {
        return mExportClassListTo;
    }

    public String getModulePackage() {
        return mModulePackage;
    }

    public boolean isLibrary() {
        return mIsLibrary;
    }

    public boolean enableDebugLogs() {
        return mEnableDebugLogs;
    }

    public boolean shouldPrintEncodedErrorLogs() {
        return mPrintEncodedErrorLogs;
    }

    public int getMinApi() {
        return mMinApi;
    }

    public Map<String, String> toMap() {
        Map<String, String> args = new HashMap<>();
        args.put(PARAM_BUILD_FOLDER, mBuildFolder);
        args.put(PARAM_AAR_OUT_FOLDER, mAarOutFolder);
        args.put(PARAM_SDK_DIR, mSdkDir);
        args.put(PARAM_XML_OUT_DIR, mXmlOutDir);
        args.put(PARAM_EXPORT_CLASS_LIST_TO, mExportClassListTo);
        args.put(PARAM_MODULE_PKG, mModulePackage);
        args.put(PARAM_MIN_API, String.valueOf(mMinApi));
        args.put(PARAM_IS_LIBRARY, serialize(mIsLibrary));
        args.put(PARAM_ENABLE_DEBUG_LOGS, serialize(mEnableDebugLogs));
        args.put(PARAM_PRINT_ENCODED_ERROR_LOGS, serialize(mPrintEncodedErrorLogs));
        return args;
    }

    private static String serialize(boolean boolValue) {
        return boolValue ? "1" : "0";
    }

    private static boolean deserialize(String boolValue) {
        return boolValue != null && "1".equals(boolValue.trim());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private File mBuildFolder;
        private File mBundleFolder;
        private File mSdkDir;
        private File mXmlOutDir;
        private File mExportClassListTo;
        private String mModulePackage;
        private Type mType;
        private Integer mMinApi;
        private boolean mEnableDebugLogs;
        private boolean mPrintEncodedErrorLogs;

        private Builder() {}
        public Builder buildFolder(File buildFolder) {
            mBuildFolder = buildFolder;
            return this;
        }
        public Builder modulePackage(String modulePackage) {
            mModulePackage = modulePackage;
            return this;
        }
        public Builder bundleFolder(File bundleFolder) {
            mBundleFolder = bundleFolder;
            return this;
        }
        public Builder sdkDir(File sdkDir) {
            mSdkDir = sdkDir;
            return this;
        }
        public Builder xmlOutDir(File xmlOutDir) {
            mXmlOutDir = xmlOutDir;
            return this;
        }
        public Builder exportClassListTo(@Nullable File exportClassListTo) {
            mExportClassListTo = exportClassListTo;
            return this;
        }
        public Builder enableDebugLogs(boolean enableDebugLogs) {
            mEnableDebugLogs = enableDebugLogs;
            return this;
        }
        public Builder type(Type type) {
            mType = type;
            return this;
        }
        public Builder printEncodedErrorLogs(boolean printEncodedErrorLogs) {
            mPrintEncodedErrorLogs = printEncodedErrorLogs;
            return this;
        }

        public Builder minApi(int minApi) {
            mMinApi = minApi;
            return this;
        }

        public DataBindingCompilerArgs build() {
            DataBindingCompilerArgs args = new DataBindingCompilerArgs();
            Preconditions.checkNotNull(mType, "Must specify type of the build. Lib or App ?"
                    + " or not");
            boolean library = mType == Type.LIBRARY;
            args.mIsLibrary = library;

            Preconditions.checkNotNull(mBuildFolder, "Must provide the build folder for data "
                    + "binding");
            args.mBuildFolder = mBuildFolder.getAbsolutePath();

            Preconditions.checkNotNull(mSdkDir, "Must provide sdk directory");
            args.mSdkDir = mSdkDir.getAbsolutePath();

            Preconditions.checkNotNull(mXmlOutDir, "Must provide xml out directory");
            args.mXmlOutDir = mXmlOutDir.getAbsolutePath();

            Preconditions.check(!library || mBundleFolder != null, "Must specify bundle folder "
                    + "(aar out folder) for library projects");
            args.mAarOutFolder = mBundleFolder.getAbsolutePath();

            Preconditions.check(!library || mExportClassListTo != null, "Must provide a folder "
                    + "to export generated class list");

            Preconditions.checkNotNull(mModulePackage, "Must provide a module package");
            args.mModulePackage = mModulePackage;


            Preconditions.checkNotNull(mMinApi, "Must provide the min api for the project");
            args.mMinApi = mMinApi;
            if (mExportClassListTo != null) {
                args.mExportClassListTo = mExportClassListTo.getAbsolutePath();
            }
            args.mEnableDebugLogs = mEnableDebugLogs;
            args.mPrintEncodedErrorLogs = mPrintEncodedErrorLogs;
            return args;
        }
    }

    public enum Type {
        LIBRARY,
        APPLICATION
    }
}
