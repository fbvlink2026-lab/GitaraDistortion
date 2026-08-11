cmake_minimum_required(VERSION 3.22.1)
project("gitaradistortion")

# ✅ I-download at ikabit ang OBOE — KUSA NANG GAGAWIN!
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

# ✅ IKABIT ANG OBOE AT IBA PA
target_link_libraries(gitaradistortion
        oboe
        android
        log
)

# ✅ SIGURADUHIN ANG C++ VERSYON
target_compile_features(gitaradistortion PRIVATE cxx_std_17)
