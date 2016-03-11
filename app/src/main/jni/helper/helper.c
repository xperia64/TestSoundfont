#include <stdio.h>
#include <jni.h>
#include <android/log.h>
#include <dlfcn.h>
#include <string.h>
//#include "timidity.h"

typedef double FLOAT_T;

#define MAX_CHANNELS 32

// So libtimidityplusplus doesn't have to have any android libs
void andro_timidity_log_print(const char *tag, const char *fmt, ...) {

    va_list listPointer;
    va_start(listPointer, fmt);
    __android_log_print(ANDROID_LOG_ERROR, tag, fmt, listPointer);
    va_end (listPointer);
}

extern int skfl_Decode(const char *InFileName, const char *ReqOutFileName);

//extern void timidity_start_initialize(void);
void (*timidity_start)(void);

//extern int timidity_pre_load_configuration(void);
int (*timidity_preload)(void);

//extern int timidity_post_load_configuration(void);
int (*timidity_postload)(void);

//extern void timidity_init_player(void);
void (*timidity_initplayer)(void);

//extern int timidity_play_main(int nfiles, char **files);
int (*timidity_play)(int, char **);

//extern int play_list(int number_of_files, char *list_of_files[]);
int (*ext_play_list)(int, char *[]);

//extern int set_current_resampler(int type);
int (*set_resamp)(int);

//extern void midi_program_change(int ch, int prog);
void (*change_prog)(int, int);

//extern void midi_volume_change(int ch, int prog);
void (*change_vol)(int, int);

//extern int droid_rc;
int *dr_rc;

//extern int droid_arg;
int *dr_arg;

//extern int got_a_configuration;
int *got_config;

//extern FLOAT_T midi_time_ratio;
FLOAT_T *time_ratio;

//extern int opt_preserve_silence;
int *preserve_silence;


char *configFile;
char *configFile2;
int sixteen;
int mono;
int itIsDone = 0;
int shouldFreeInsts = 1;
//JNIEnv* envelope;
//JavaVM  *jvm;
//JNIEnv *theGoodEnv;
static jclass pushClazz;
static jmethodID pushBuffit;
static jmethodID flushId;
static jmethodID buffId;
static jmethodID controlId;
static jmethodID rateId;
static jmethodID finishId;
static jmethodID seekInitId;
static jmethodID updateSeekId;
static jmethodID pushLyricId;
static jmethodID updateMaxChanId;
static jmethodID updateProgId;
static jmethodID updateVolId;
static jmethodID updateDrumId;
static jmethodID updateTempoId;
static jmethodID updateMaxVoiceId;
static jmethodID updateKeyId;
static JavaVM *mJavaVM;

static int libsLoaded = 0;
static void *libHandle;
static int badState = 0;

static void Android_JNI_ThreadDestroyed(void *value) {
    /* The thread is being destroyed, detach it from the Java VM and set the mThreadKey value to NULL as required */
    JNIEnv *env = (JNIEnv *) value;
    if (env != NULL) {
        (*mJavaVM)->DetachCurrentThread(mJavaVM);
    }
}

static JNIEnv *Android_JNI_GetEnv(void) {
    /* From http://developer.android.com/guide/practices/jni.html
     * All threads are Linux threads, scheduled by the kernel.
     * They're usually started from managed code (using Thread.start), but they can also be created elsewhere and then
     * attached to the JavaVM. For example, a thread started with pthread_create can be attached with the
     * JNI AttachCurrentThread or AttachCurrentThreadAsDaemon functions. Until a thread is attached, it has no JNIEnv,
     * and cannot make JNI calls.
     * Attaching a natively-created thread causes a java.lang.Thread object to be constructed and added to the "main"
     * ThreadGroup, making it visible to the debugger. Calling AttachCurrentThread on an already-attached thread
     * is a no-op.
     * Note: You can call this function any number of times for the same thread, there's no harm in it
     */

    JNIEnv *env;
    int status = (*mJavaVM)->GetEnv(mJavaVM, (void **) &env, JNI_VERSION_1_4);
    if (status == JNI_EDETACHED) {
        //LOGI("GetEnv: not attached");
        if ((*mJavaVM)->AttachCurrentThread(mJavaVM, &env, NULL) != 0) {
            //LOGE("Failed to attach");
        }
    }
    return env;
}

static int Android_JNI_SetupThread(void) {
    /* From http://developer.android.com/guide/practices/jni.html
     * Threads attached through JNI must call DetachCurrentThread before they exit. If coding this directly is awkward,
     * in Android 2.0 (Eclair) and higher you can use pthread_key_create to define a destructor function that will be
     * called before the thread exits, and call DetachCurrentThread from there. (Use that key with pthread_setspecific
     * to store the JNIEnv in thread-local-storage; that way it'll be passed into your destructor as the argument.)
     * Note: The destructor is not called unless the stored value is != NULL
     * Note: You can call this function any number of times for the same thread, there's no harm in it
     *       (except for some lost CPU cycles)
     */
    JNIEnv *env = Android_JNI_GetEnv();
    return 1;
}

extern jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    mJavaVM = vm;
    if ((*mJavaVM)->GetEnv(mJavaVM, (void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }
    /*
     * Create mThreadKey so we can keep track of the JNIEnv assigned to each thread
     * Refer to http://developer.android.com/guide/practices/design/jni.html for the rationale behind this
     */
    Android_JNI_SetupThread();

    return JNI_VERSION_1_4;
}

extern void JNI_OnUnload(JavaVM *vm, void *reserved) {
    JNIEnv *env = Android_JNI_GetEnv();
    (*env)->DeleteGlobalRef(env, pushClazz);
}


int checkLibError() {
    const char *error = dlerror();
    if (error) {
        __android_log_print(ANDROID_LOG_DEBUG, "TIMIDITY", "%s", error);
        return 1;
    }
    return 0;
}

JNIEXPORT int JNICALL
Java_com_example_testsoundfont_JNIHandler_loadLib(JNIEnv *env, jobject obj, jstring path) {
    if (!libsLoaded) {
        jboolean isCopy;
        char *libPath = (char *) (*env)->GetStringUTFChars(env, path, &isCopy);
        dlerror();
        libHandle = dlopen(libPath, RTLD_NOW);

        if (checkLibError()) {
            return -1;
        }
        timidity_start = dlsym(libHandle, "timidity_start_initialize");
        if (checkLibError()) {
            return -2;
        }
        timidity_preload = dlsym(libHandle, "timidity_pre_load_configuration");
        if (checkLibError()) {
            return -3;
        }
        timidity_postload = dlsym(libHandle, "timidity_post_load_configuration");
        if (checkLibError()) {
            return -4;
        }
        timidity_initplayer = dlsym(libHandle, "timidity_init_player");
        if (checkLibError()) {
            return -5;
        }
        timidity_play = dlsym(libHandle, "timidity_play_main");
        if (checkLibError()) {
            return -6;
        }
        ext_play_list = dlsym(libHandle, "play_list");
        if (checkLibError()) {
            return -7;
        }
        set_resamp = dlsym(libHandle, "set_current_resampler");
        if (checkLibError()) {
            return -8;
        }
        change_prog = dlsym(libHandle, "midi_program_change");
        if (checkLibError()) {
            return -9;
        }
        change_vol = dlsym(libHandle, "midi_volume_change");
        if (checkLibError()) {
            return -10;
        }
        dr_rc = dlsym(libHandle, "droid_rc");
        if (checkLibError()) {
            return -11;
        }
        dr_arg = dlsym(libHandle, "droid_arg");
        if (checkLibError()) {
            return -12;
        }
        got_config = dlsym(libHandle, "got_a_configuration");
        if (checkLibError()) {
            return -13;
        }
        time_ratio = dlsym(libHandle, "midi_time_ratio");
        if (checkLibError()) {
            return -14;
        }
        preserve_silence = dlsym(libHandle, "opt_preserve_silence");
        if (checkLibError()) {
            return -15;
        }
        libsLoaded = 1;
        (*env)->ReleaseStringUTFChars(env, path, libPath);
        return 0;
    }
    return 1;
}

JNIEXPORT int JNICALL
Java_com_example_testsoundfont_JNIHandler_unloadLib(JNIEnv *env, jobject obj) {
    if (libsLoaded && !libHandle) {
        return -1; // nothing to do
    }
    int libclose = dlclose(libHandle);
    if (libclose != 0) {
        __android_log_print(ANDROID_LOG_DEBUG, "TIMIDITY", "Couldn't unload %d", libclose);
    } else {
        libsLoaded = 0;
    }
    checkLibError();
    return 0;
}


/**
 * config : timidity.cfg의 절대경로 상위폴더
 * config2 : tmidity.cfg의 절대경로
 * mono : 뭔지모름. 일단 0으로 고정
 * custResamp : 리샘플링타입. 0으로 고정
 * sixteen : 뭔지모름. true로 고정
 * PreSil : 뭔지모름. true로 고정
 * reloading : 뭔지모름. true로 고정
 * freeInst : 뭔지모름. true로 고정
 */
JNIEXPORT int JNICALL
Java_com_example_testsoundfont_JNIHandler_prepareTimidity(JNIEnv *env, jobject obj, jstring config,
                                                          jstring config2, jint jmono,
                                                          jint jcurrentResampleType, jint jsixteen,
                                                          jint jPresSil, jint jreloading,
                                                          jint jfreeInsts) {
    itIsDone = 0;
    if (!jreloading) {
        Android_JNI_SetupThread();
        //jclass tmp =
        //pushClazz = (jclass)(*env)->NewGlobalRef(env, tmp);
        pushClazz = (*env)->NewGlobalRef(env, (*env)->FindClass(env,
                                                                "com/example/testsoundfont/JNIHandler"));
        pushBuffit = (*env)->GetStaticMethodID(env, pushClazz, "buffit", "([BI)V");
        flushId = (*env)->GetStaticMethodID(env, pushClazz, "flushIt", "()V");
        buffId = (*env)->GetStaticMethodID(env, pushClazz, "bufferSize", "()I");
        controlId = (*env)->GetStaticMethodID(env, pushClazz, "controlMe", "(I)V");
        buffId = (*env)->GetStaticMethodID(env, pushClazz, "bufferSize", "()I");
        rateId = (*env)->GetStaticMethodID(env, pushClazz, "getRate", "()I");
        finishId = (*env)->GetStaticMethodID(env, pushClazz, "finishIt", "()V");
        seekInitId = (*env)->GetStaticMethodID(env, pushClazz, "initSeeker", "(I)V");
        updateSeekId = (*env)->GetStaticMethodID(env, pushClazz, "updateSeeker", "(II)V");
        pushLyricId = (*env)->GetStaticMethodID(env, pushClazz, "updateLyrics", "([B)V");
        updateMaxChanId = (*env)->GetStaticMethodID(env, pushClazz, "updateMaxChannels", "(I)V");
        updateProgId = (*env)->GetStaticMethodID(env, pushClazz, "updateProgramInfo", "(II)V");
        updateVolId = (*env)->GetStaticMethodID(env, pushClazz, "updateVolInfo", "(II)V");
        updateDrumId = (*env)->GetStaticMethodID(env, pushClazz, "updateDrumInfo", "(II)V");
        updateTempoId = (*env)->GetStaticMethodID(env, pushClazz, "updateTempo", "(II)V");
        updateMaxVoiceId = (*env)->GetStaticMethodID(env, pushClazz, "updateMaxVoice", "(I)V");
        updateKeyId = (*env)->GetStaticMethodID(env, pushClazz, "updateKey", "(I)V");
    }

    mono = (int) jmono;
    sixteen = (int) jsixteen;
    shouldFreeInsts = (int) jfreeInsts;
    jboolean isCopy;
    configFile = (char *) (*env)->GetStringUTFChars(env, config, &isCopy);
    configFile2 = (char *) (*env)->GetStringUTFChars(env, config2, &isCopy);
    int err = 0;
    //timidity_start_initialize();
    (*timidity_start)();

    if ((err = (*timidity_preload)()) != 0)
        return err;
    err += (*timidity_postload)();
    if (err) {
        return -121;
    }

    *preserve_silence = (int) jPresSil;
    //__android_log_print(ANDROID_LOG_DEBUG, "TIMIDITY", "Preserve Silence: %d %d", *preserve_silence, jPresSil);
    (*timidity_initplayer)();
    (*set_resamp)(jcurrentResampleType);
    (*env)->ReleaseStringUTFChars(env, config, configFile);
    (*env)->ReleaseStringUTFChars(env, config2, configFile2);

    return 0;
}

void setMaxChannels(int ca) {
    JNIEnv *theGoodEnv = Android_JNI_GetEnv();
    (*theGoodEnv)->CallStaticVoidMethod(theGoodEnv, pushClazz, updateMaxChanId, ca);
}

void finishAE() {
    JNIEnv *theGoodEnv = Android_JNI_GetEnv();
    (*theGoodEnv)->CallStaticVoidMethod(theGoodEnv, pushClazz, finishId);
    //exit(0); //? do nothing
}

JNIEXPORT int JNICALL
Java_com_example_testsoundfont_JNIHandler_loadSongTimidity(JNIEnv *env, jobject obj, jstring song) {
    // It would appear we have to do the following code every time a song is loaded
    // Don't you just love JNI+threading?

    // Must be called once to open output. Thank you mac_main for the NULL file list thing
    if (!itIsDone) {
        setMaxChannels((int) MAX_CHANNELS);
        (*timidity_play)(0, NULL);
        itIsDone = 1;
    }
    int main_ret;
    char *filez[1];
    jboolean isCopy;
    //filez = malloc(sizeof(char*) * 1);
    filez[0] = (char *) (*env)->GetStringUTFChars(env, song, &isCopy);
    //main_ret = timidity_play_main(1, filez);
    (*ext_play_list)(1, filez);
    (*env)->ReleaseStringUTFChars(env, song, filez[0]);
    finishAE();

    //(*theGoodEnv)->DeleteLocalRef(theGoodEnv, pushClazz);
    return 0;

}

JNIEXPORT int JNICALL
Java_com_example_testsoundfont_JNIHandler_setResampleTimidity(JNIEnv *env, jobject obj,
                                                              jint jcustResamp) {
    return (*set_resamp)(jcustResamp);
}

JNIEXPORT int JNICALL
Java_com_example_testsoundfont_JNIHandler_decompressSFArk(JNIEnv *env, jobject obj, jstring jfrom,
                                                          jstring jto) {
    jboolean isCopy;
    const char *from = (*env)->GetStringUTFChars(env, jfrom, &isCopy);
    const char *to = (*env)->GetStringUTFChars(env, jto, &isCopy);
    int x = sfkl_Decode(from, to);
    (*env)->ReleaseStringUTFChars(env, jfrom, from);
    (*env)->ReleaseStringUTFChars(env, jto, to);
    return x;
}

JNIEXPORT void JNICALL
Java_com_example_testsoundfont_JNIHandler_setChannelTimidity(JNIEnv *env, jobject obj, jint jchan,
                                                             jint jprog) {
    (*change_prog)((int) jchan, (int) jprog);
}

JNIEXPORT void JNICALL
Java_com_example_testsoundfont_JNIHandler_setChannelVolumeTimidity(JNIEnv *env, jobject obj,
                                                                   jint jchan, jint jvol) {
    (*change_vol)((int) jchan, (int) jvol);
}

char *getConfig() {
    //__android_log_print(ANDROID_LOG_DEBUG, "TIMIDITY", "%s", configFile);
    return configFile;
}

char *getConfig2() {
    //__android_log_print(ANDROID_LOG_DEBUG, "TIMIDITY", "%s", configFile2);
    return configFile2;
}

int getFreeInsts() {
    //__android_log_print(ANDROID_LOG_DEBUG, "TIMIDITY", "%d", shouldFreeInsts);
    return shouldFreeInsts;
}

int nativePush(char *buf, int nframes) {
    //jclass clazz = (*theGoodEnv)->FindClass(theGoodEnv, "com/xperia64/timidityae/JNIHandler");
    JNIEnv *theGoodEnv = Android_JNI_GetEnv();
    //jmethodID buffit = (*theGoodEnv)->GetStaticMethodID(theGoodEnv, clazz, "buffit", "([BI)V");
    jbyteArray byteArr = (*theGoodEnv)->NewByteArray(theGoodEnv, nframes);
    (*theGoodEnv)->SetByteArrayRegion(theGoodEnv, byteArr, 0, nframes, (jbyte *) buf);
    (*theGoodEnv)->CallStaticVoidMethod(theGoodEnv, pushClazz, pushBuffit, byteArr, nframes);
    (*theGoodEnv)->DeleteLocalRef(theGoodEnv, byteArr);
    //(*theGoodEnv)->DeleteLocalRef(theGoodEnv, clazz);

    return 0;
}

JNIEXPORT void JNICALL
Java_com_example_testsoundfont_JNIHandler_controlTimidity(JNIEnv *env, jobject obj, jint jcmd,
                                                          jint jcmdArg) {
    (*dr_rc) = (int) jcmd;
    (*dr_arg) = (int) jcmdArg;
    if ((*dr_rc) == 6) // When else are samples even used w/JNI?
    {
        (*dr_arg) *= (int) ((*time_ratio) *
                            getSampleRate()); // I'm not syncing that nasty float to the java side.
    }
}

JNIEXPORT jboolean JNICALL
Java_com_example_testsoundfont_JNIHandler_timidityReady(JNIEnv *env, jobject obj) {
    return ((*dr_rc) ? JNI_FALSE : JNI_TRUE);
}

void flushIt() {
    JNIEnv *theGoodEnv = Android_JNI_GetEnv();
    //jclass clazz = (*theGoodEnv)->FindClass(theGoodEnv, "com/xperia64/timidityae/JNIHandler");
    //jmethodID buffit = (*theGoodEnv)->GetStaticMethodID(theGoodEnv, pushClazz, "flushIt", "()V");
    (*theGoodEnv)->CallStaticIntMethod(theGoodEnv, pushClazz, flushId);
    //(*theGoodEnv)->DeleteLocalRef(theGoodEnv, clazz);
}

int getBuffer() {
    JNIEnv *theGoodEnv = Android_JNI_GetEnv();
    //jclass clazz = (*theGoodEnv)->FindClass(theGoodEnv, "com/xperia64/timidityae/JNIHandler");
    //jmethodID buffit = (*theGoodEnv)->GetStaticMethodID(theGoodEnv, clazz, "bufferSize", "()I");
    int r = (int) (*theGoodEnv)->CallStaticIntMethod(theGoodEnv, pushClazz, buffId);
    //(*theGoodEnv)->DeleteLocalRef(theGoodEnv, clazz);
    return r;
}

int getMono() {
    return mono;
}

int getSixteen() {
    return sixteen;
}

/*int pollForControl()
{
	int tmp = controlCode;
	controlCode=0;
	return tmp;
}
void setControl(int x)
{
	controlCode = x;
}
int getControlArg()
{
	int tmp = controlArg;
	controlArg=0;
	return tmp;
}*/
void setMaxTime(int time) {
    JNIEnv *theGoodEnv = Android_JNI_GetEnv();
    (*theGoodEnv)->CallStaticVoidMethod(theGoodEnv, pushClazz, seekInitId, time);
}

void setCurrTime(int time, int v) {
    JNIEnv *theGoodEnv = Android_JNI_GetEnv();
    (*theGoodEnv)->CallStaticVoidMethod(theGoodEnv, pushClazz, updateSeekId, time, v);
}

void controller(int aa) {
    JNIEnv *theGoodEnv = Android_JNI_GetEnv();
    //jclass clazz = (*theGoodEnv)->FindClass(theGoodEnv, "com/xperia64/timidityae/JNIHandler");
    //jclass cls = (*envelope)->GetObjectClass(envelope, mine);
    //jmethodID buffit = (*theGoodEnv)->GetStaticMethodID(theGoodEnv, clazz, "controlMe", "(I)V");
    (*theGoodEnv)->CallStaticVoidMethod(theGoodEnv, pushClazz, controlId, aa);
    //(*theGoodEnv)->DeleteLocalRef(theGoodEnv, clazz);
}

int getSampleRate() {
    JNIEnv *theGoodEnv = Android_JNI_GetEnv();
    return (*theGoodEnv)->CallStaticIntMethod(theGoodEnv, pushClazz, rateId);
}

void setCurrLyric(char *lyric) {
    JNIEnv *theGoodEnv = Android_JNI_GetEnv();
    jbyteArray byteArr = (*theGoodEnv)->NewByteArray(theGoodEnv, 300);
    (*theGoodEnv)->SetByteArrayRegion(theGoodEnv, byteArr, 0, 300, (jbyte *) lyric);
    (*theGoodEnv)->CallStaticVoidMethod(theGoodEnv, pushClazz, pushLyricId, byteArr, 300);
    (*theGoodEnv)->DeleteLocalRef(theGoodEnv, byteArr);
}

void setProgram(int ch, int prog) {
    JNIEnv *theGoodEnv = Android_JNI_GetEnv();
    (*theGoodEnv)->CallStaticVoidMethod(theGoodEnv, pushClazz, updateProgId, ch, prog);
}

void setVol(int ch, int vol) {
    JNIEnv *theGoodEnv = Android_JNI_GetEnv();
    (*theGoodEnv)->CallStaticVoidMethod(theGoodEnv, pushClazz, updateVolId, ch, vol);
}

void setDrum(int ch, int isDrum) {
    JNIEnv *theGoodEnv = Android_JNI_GetEnv();
    (*theGoodEnv)->CallStaticVoidMethod(theGoodEnv, pushClazz, updateDrumId, ch, isDrum);
}

void sendTempo(int t, int tr) {
    JNIEnv *theGoodEnv = Android_JNI_GetEnv();
    (*theGoodEnv)->CallStaticVoidMethod(theGoodEnv, pushClazz, updateTempoId, t, tr);
}

void sendKey(int k) {
    JNIEnv *theGoodEnv = Android_JNI_GetEnv();
    (*theGoodEnv)->CallStaticVoidMethod(theGoodEnv, pushClazz, updateKeyId, k);
}

void sendMaxVoice(int mv) {
    JNIEnv *theGoodEnv = Android_JNI_GetEnv();
    (*theGoodEnv)->CallStaticVoidMethod(theGoodEnv, pushClazz, updateMaxVoiceId, mv);
}
