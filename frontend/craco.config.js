const webpack = require('webpack');

// 禁用弃用警告的环境变量
process.env.DANGEROUSLY_DISABLE_HOST_CHECK = 'true';

module.exports = {
  webpack: {
    // 添加进度条插件以显示构建进度
    plugins: [
      new webpack.ProgressPlugin()
    ],
    
    // 优化解析配置
    resolve: {
      extensions: ['.tsx', '.ts', '.jsx', '.js', '.json'],
      cacheWithContext: false,
    },
    
    // 优化开发环境配置
    cache: {
      type: 'filesystem',
      buildDependencies: {
        config: [__filename],
      },
    },
    
    // 优化source map配置
    devtool: 'eval-cheap-module-source-map',
  },
  
  devServer: {
    port: process.env.PORT || 9180,
    open: false,
    client: {
      overlay: {
        errors: true,
        warnings: false,
      },
    },
    
    // 使用新的中间件配置方式
    setupMiddlewares: (middlewares, devServer) => {
      if (!devServer) {
        throw new Error('webpack-dev-server is not defined');
      }
      
      // 这里可以添加自定义中间件
      
      return middlewares;
    },
    
    // 禁用旧的中间件配置
    onAfterSetupMiddleware: undefined,
    onBeforeSetupMiddleware: undefined,
  },
  
  // 配置babel
  babel: {
    presets: [
      ['@babel/preset-react', { runtime: 'automatic', importSource: 'react' }]
    ]
  },
};