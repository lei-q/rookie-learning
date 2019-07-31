//可以抽取出常量
        String lockUniquePrefix = "lock";

        Jedis jedis = getJedis();
        //1. 获取锁
        // key:"lock"+方法名,value:时间戳
        //NX -- Only set the key if it does not already exist.
        String key = lockUniquePrefix + Thread.currentThread().getStackTrace()[1].getMethodName();
        Integer userId = 222;
        String conventionK = requestSafeThreadParamDto.getCid();
        //不同的两个用户同时提交订单,是允许并发的,这种情况不应该使用锁机制来限制,
        //所以我们使用分布式锁限制的只是 两次请求信息完全相同的两次请求,
        //造成这种两次完全相同的请求的原因,可能是网络卡顿导致用户重复点击,或者nginx 的重发
        String hashSource = WebServletUtil.buildHashSource(request, userId, conventionK);
        //对请求信息 做hash
        long crc32Long = EncryptionUtil.getHash(hashSource);
        //"OK":成功;null:失败
        String result = jedis.set(key + crc32Long, "aa", "NX", "EX"/*seconds*/, 1000);
        Const.pool.returnResource(jedis);
        boolean success = "OK".equals(result);
        System.out.println("success :" + success);
        System.out.println("result :" + result);

        try {
            if (success) {
                //2. 执行具体业务逻辑
                //...
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //3. 业务逻辑执行完成之后,释放锁
            if (null != del && del) {
                jedis = getJedis();
                jedis.del(key);
                Const.pool.returnResource(jedis);
            }
        }



//        return new BaseResponseDto(true).setValue(result).toJson();
        return BaseResponseDto.put2("result", result).put("success", success).toJson();