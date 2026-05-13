@org.springframework.modulith.ApplicationModule(displayName = "Community", // 커뮤니티 모듈은 shared모듈만 의존할수있다.
        allowedDependencies = { "shared" })
package com.synapse.engagement.community;
