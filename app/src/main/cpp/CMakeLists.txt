cmake_minimum_required(VERSION 3.22.1)
project("gitaradistortion")

add_library(
        gitaradistortion SHARED
        AudioEngine.cpp
)

find_package(oboe REQUIRED CONFIG)

target_link_libraries(gitaradistortion
        oboe::oboe
        android
        log
)

target_compile_features(gitaradistortion PRIVATE cxx_std_17)
