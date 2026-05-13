@org.springframework.modulith.ApplicationModule(displayName = "Gamification", // Gamification 모듈은 shared 모듈만 의존할수있다.
        allowedDependencies = { "shared" })
package com.synapse.engagement.gamification;
