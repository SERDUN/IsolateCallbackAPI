package com.serdun.online.isolate_tester.services


enum class TestForegroundCallServiceEnums {
    Launch, WakeUp, Teardown;

    val action: String
        get() = "1234" + name + "_foreground_call_service"
}
