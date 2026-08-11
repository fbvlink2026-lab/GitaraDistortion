cmake_minimum_required(VERSION 3.22.1)
project("gitaradistortion")

# ✅ KUSA NANG I-DOWNLOAD ANG OBOE — WALANG HIHIRAP!
include(FetchContent)
FetchContent_Declare(
        oboe
        GIT_REPOSITORY https://github.com/google/oboe.git
        GIT_TAG 1.7.0
)
FetchContent_MakeAvailable(oboe)

# ✅ ANG ATING AUDIO CODE
add_library(
        gitaradistortion SHARED
        AudioEngine.cpp
)

# ✅ IKABIT ANG OBOE
target_link_libraries(gitaradistortion
        oboe::oboe
        android
        log
)

target_compile_features(gitaradistortion PRIVATE cxx_std_17)
