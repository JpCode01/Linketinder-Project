const gulp = require("gulp");
const clean = require("gulp-clean");
const webpackStream = require("webpack-stream");

const webpackConfig = require("./webpack.config");

function limpar() {
    return gulp
        .src("./dist", { read: false, allowEmpty: true })
        .pipe(clean());
}

function html() {
    return gulp
        .src("./src/index.html")
        .pipe(gulp.dest("./dist"));
}

function css() {
    return gulp
        .src("./src/css/**/*.css")
        .pipe(gulp.dest("./dist"));
}

function javascript() {
    return gulp
        .src("./src/ts/main.ts")
        .pipe(webpackStream(webpackConfig))
        .pipe(gulp.dest("./dist"));
}

const build = gulp.series(
    limpar,
    gulp.parallel(
        html,
        css,
        javascript
    )
);

exports.clean = limpar;
exports.build = build;
exports.default = build;