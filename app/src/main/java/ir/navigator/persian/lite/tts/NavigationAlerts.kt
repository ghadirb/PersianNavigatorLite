package ir.navigator.persian.lite.tts

/**
 * انواع هشدارهای ناوبری
 */
enum class NavigationAlert {
    TURN_LEFT,              // به چپ بپیچید
    TURN_RIGHT,             // به راست بپیچید
    TURN_LEFT_SOON,         // به زودی به چپ بپیچید
    TURN_RIGHT_SOON,        // به زودی به راست بپیچید
    TURN_LEFT_100M,         // در 100 متر به چپ بپیچید
    TURN_RIGHT_100M,        // در 100 متر به راست بپیچید
    TURN_LEFT_200M,         // در 200 متر به چپ بپیچید
    TURN_RIGHT_200M,        // در 200 متر به راست بپیچید
    TURN_LEFT_500M,         // در 500 متر به چپ بپیچید
    TURN_RIGHT_500M,        // در 500 متر به راست بپیچید
    CONTINUE_ROUTE,         // مسیر را ادامه دهید
    MAKE_U_TURN,            // دور بزنید
    U_TURN_100M,            // صد متر دیگر دور بزنید
    U_TURN_300M,            // سیصد متر دیگر دور بزنید
    ROUNDABOUT_EXIT_1,      // در میدان از خروجی اول خارج شوید
    ROUNDABOUT_EXIT_2,      // در میدان از خروجی دوم خارج شوید
    ROUNDABOUT_EXIT_3,      // در میدان از خروجی سوم خارج شوید
    DESTINATION_ARRIVED     // به مقصد رسیدید
}

/**
 * انواع هشدارهای سرعت
 */
enum class SpeedAlert {
    REDUCE_SPEED,           // سرعت خود را کاهش دهید
    SPEEDING_DANGER,        // خطر! سرعت غیر مجاز
    SPEED_CAMERA,           // دوربین کنترل سرعت
    SPEED_LIMIT_ATTENTION,  // توجه به محدودیت سرعت
    SPEED_LIMIT_30,         // محدودیت سرعت 30 کیلومتر
    SPEED_LIMIT_60,         // محدودیت سرعت 60 کیلومتر
    SPEED_LIMIT_80,         // محدودیت سرعت 80 کیلومتر
    SPEED_LIMIT_90,         // محدودیت سرعت 90 کیلومتر
    SPEED_LIMIT_110,        // محدودیت سرعت 110 کیلومتر
    SPEED_LIMIT_120         // محدودیت سرعت 120 کیلومتر
}

/**
 * انواع هشدارهای عمومی
 */
enum class GeneralAlert {
    DANGER_AHEAD,           // احتیاط! خطر در پیش است
    STOP_AHEAD,             // ایستگاه توقف در پیش است
    HEAVY_TRAFFIC,          // ترافیک سنگین در پیش است
    ALTERNATIVE_ROUTE,      // مسیر جایگزین پیشنهاد می شود
    DELAY_10_MIN,           // تأخیر در مسیر 10 دقیقه
    FUEL_STATION_1KM,       // سوخت گیری در 1 کیلومتری
    FUEL_STATION_5KM,       // سوخت گیری در 5 کیلومتری
    PARKING_NEARBY          // پارکینگ در نزدیکی شما
}
